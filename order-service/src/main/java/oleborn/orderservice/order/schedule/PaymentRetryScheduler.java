package oleborn.orderservice.order.schedule;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oleborn.orderservice.order.dictionary.OrderStatus;
import oleborn.orderservice.order.domain.Order;
import oleborn.orderservice.order.domain.dto.OrderCreatedEvent;
import oleborn.orderservice.order.domain.dto.PaymentResponseDto;
import oleborn.orderservice.order.repository.OrderRepository;
import oleborn.orderservice.order.service.PaymentService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRetryScheduler {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final MeterRegistry meterRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${payment.retry.max-attempt:3}")
    private Integer maxAttempts;

    @Value("${payment.retry.timeout:30}")
    private Long timeout;

    @Value("${payment.retry.batch-size:100}")
    private Integer batchSize;

    private final Map<Long, Integer> retryCounts = new ConcurrentHashMap<>();

    @Scheduled(
            fixedDelayString = "${payment.scanner.fixed-delay:30000}",
            initialDelayString = "${payment.scanner.initial-delay:30000}"
    )
    @Transactional
    public void scanAndProcessPendingOrders() {

        log.debug("Старт поиска заказов с статусом PENDING");

        Pageable pageable = PageRequest.of(0, batchSize);

        List<Order> byStatus = orderRepository.findByStatus(OrderStatus.PENDING, pageable);

        if (byStatus.isEmpty()) {
            log.debug("Нет заказов в статусе PENDING");
            return;
        }

        log.debug("Найдено {} заказов в статусе PENDING", byStatus.size());

        for (Order order : byStatus) {
            try {
                processOrder(order);
            } catch (Exception e) {
                log.error("Ошибка при обработке заказа: {}", e.getMessage());
            }
        }

    }

    private void processOrder(Order order) {

        int attempts = retryCounts.getOrDefault(order.getId(), 0);

        if (attempts >= maxAttempts) {
            log.debug("Заказ id: {}, исчерпал все попытки. Отмена.", order.getId());

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            retryCounts.remove(order.getId());

            return;
        }

        log.debug("Попытка {}/{} оплатить заказ {}", attempts + 1, maxAttempts, order.getId());

        try {
            PaymentResponseDto responseDto = paymentService.processPaymentResilience4j(order)
                    .get(timeout, TimeUnit.SECONDS);

            if (responseDto.isRequiredPendingProcess()) {
                retryCounts.put(order.getId(), attempts + 1);

                log.warn("Заказ {} не оплачен(попытка {}). Сервис не доступен.", order.getId(), attempts + 1);

                recordPendingMetrics(attempts + 1);

            } else if (responseDto.isSuccessful()) {

                try {

                    MDC.put("order_id", order.getId().toString());
                    MDC.put("total_amount", order.getTotalPrice().toString());
                    MDC.put("order_status", order.getStatus().toString());

                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);

                    log.info("Заказ {} успешно оплачен с {}-й попытки", order.getId(), attempts + 1);

                    retryCounts.remove(order.getId());

                    eventPublisher.publishEvent(OrderCreatedEvent.of(
                                    order.getId(),
                                    MDC.getCopyOfContextMap()
                            )
                    );
                } finally {
                    MDC.remove("order_id");
                    MDC.remove("total_amount");
                    MDC.remove("order_status");
                }

            } else {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.saveAndFlush(order);

                log.warn("Бизнес ошибка оплаты заказа id: {}, причина: {}", order.getId(), responseDto.message());

                retryCounts.remove(order.getId());
            }
        }  catch (Exception e) {
            retryCounts.put(order.getId(), attempts + 1);
            log.error("Бизнес ошибка заказа {}, попытка {}, причина {}", order.getId(), attempts, e.getMessage());
        }
    }

    private void recordPendingMetrics(int attempts) {
        meterRegistry.counter("payment.retry.pending").increment(attempts);
    }
}

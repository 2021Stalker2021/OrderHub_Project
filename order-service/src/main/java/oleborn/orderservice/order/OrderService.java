package oleborn.orderservice.order;

import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import oleborn.orderservice.order.domain.Order;
import oleborn.orderservice.order.domain.OrderItem;
import oleborn.orderservice.order.domain.event.OrderCreateEvent;
import oleborn.orderservice.order.exception.NotFoundOrderException;
import oleborn.orderservice.order.metrics.annotation.BusinessMetric;
import oleborn.orderservice.order.repository.OrderRepository;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @BusinessMetric(
            value = "orders.created",
            tags = {"operation=create", "type=write"}
    )
    @Observed(name = "order.creation", contextualName = "create-order")
    @SneakyThrows
    public Order createOrder(CreateOrderRequest request) {

        log.debug("В метод createOrder получен запрос {}", request);

        int random = new Random().nextInt(100);

        log.info("Выпало число: {}", random);

        if (random < 30) {
            log.error("Возникли проблемы с обработкой сохранения заказа");
            throw new RuntimeException("Типа проблемы с обработкой заказа");
        }

        if (random > 70) {
            log.warn("OrderService замедлился");
            Thread.sleep(200);
        }

        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.price()
                ))
                .toList();

        Order order = new Order(items);

        Order savedOrder = orderRepository.saveAndFlush(order);

        log.info("Отправляем инфо о заказе, id: {}", savedOrder.getId());

        try {
            MDC.put("order_id", savedOrder.getId().toString());
            MDC.put("total_amount", savedOrder.getTotalPrice().toString());
            MDC.put("order_status", savedOrder.getStatus().toString());

            eventPublisher.publishEvent(OrderCreateEvent.of(
                    savedOrder.getId(),
                    MDC.getCopyOfContextMap()
                    )
            );

            log.debug("Все успешно сохранено");

            Span.current().setAttribute("order.id", savedOrder.getId());

            return savedOrder;

        } finally {
            MDC.remove("order_id");
            MDC.remove("total_amount");
            MDC.remove("order_status");
        }
    }

    @Transactional(readOnly = true)
    @BusinessMetric(
            value = "orders.retrieved",
            tags = {"operation=get", "type=read"}
    )
    public Order findOrderById(Long id) {

        log.debug("В метод findOrderById получен запрос поиска по id: {}", id);

        Order order = orderRepository.findWithItemsById(id).orElseThrow(
                () -> new NotFoundOrderException("Order not found")
        );

        log.debug("Результат успешно найден");

        return order;
    }
}

package oleborn.orderservice.order;

import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.trace.Span;
import oleborn.orderservice.order.domain.Order;
import oleborn.orderservice.order.domain.OrderItem;
import oleborn.orderservice.order.domain.event.OrderCreateEvent;
import oleborn.orderservice.order.exception.NotFoundOrderException;
import oleborn.orderservice.order.metrics.annotation.BusinessMetric;
import oleborn.orderservice.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Order createOrder(CreateOrderRequest request) {

        log.debug("В метод createOrder получен запрос {}", request);

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

        log.info("Заказ подготовлен к отправке id: {}", order.getId());

        eventPublisher.publishEvent(OrderCreateEvent.of(savedOrder.getId()));

        log.debug("Все успешно сохранено");

        Span.current().setAttribute("order.id", savedOrder.getId());

        return savedOrder;
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

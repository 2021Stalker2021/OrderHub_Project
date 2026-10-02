package oleborn.orderservice.order;

import oleborn.orderservice.order.exception.NotFoundOrderException;
import oleborn.orderservice.order.metrics.annotation.BusinessMetric;
import oleborn.orderservice.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @BusinessMetric(
            value = "orders.created",
            tags = {"operation=create", "type=write"}
    )
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

        log.debug("Все успешно сохранено");

        return orderRepository.save(order);
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

package gsc.edward.OrderHub_Project.order;

import gsc.edward.OrderHub_Project.order.exception.NotFoundOrderException;
import gsc.edward.OrderHub_Project.order.repository.OrderRepository;
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
    public Order findOrderById(Long id) {

        log.debug("В метод findOrderById получен запрос поиска по id: {}", id);

        Order order = orderRepository.findWithItemsById(id).orElseThrow(
                () -> new NotFoundOrderException("Order not found")
        );

        log.debug("Результат успешно найден");

        return order;
    }
}

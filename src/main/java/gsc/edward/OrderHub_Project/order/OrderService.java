package gsc.edward.OrderHub_Project.order;

import gsc.edward.OrderHub_Project.order.exception.NotFoundOrderException;
import gsc.edward.OrderHub_Project.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(CreateOrderRequest request) {
        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.price()
                ))
                .toList();

        Order order = new Order(items);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order findOrderById(Long id) {
        return orderRepository.findWithItemsById(id).orElseThrow(
                () -> new NotFoundOrderException("Заказ не найден")
        );
    }
}

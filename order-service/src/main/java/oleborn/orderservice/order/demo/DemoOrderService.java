package oleborn.orderservice.order.demo;

import gsc.edward.OrderHub_Project.order.CreateOrderRequest;
import gsc.edward.OrderHub_Project.order.Order;
import gsc.edward.OrderHub_Project.order.OrderItem;
import gsc.edward.OrderHub_Project.order.demo.repository.JdbcOrderRepository;
import gsc.edward.OrderHub_Project.order.demo.repository.JooqOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DemoOrderService {

    private final JdbcOrderRepository jdbcOrderRepository;
    private final JooqOrderRepository jooqOrderRepository;

    public Order createWithJdbc(CreateOrderRequest createOrderRequest) {
        List<OrderItem> items = createOrderRequest.items().stream()
                .map(
                        item -> new OrderItem(
                                item.productId(),
                                item.productName(),
                                item.quantity(),
                                item.price()
                        )
                )
                .toList();

        Order order = new Order(items);

        return jdbcOrderRepository.save(order);
    }

    public Order createWithJooq(CreateOrderRequest createOrderRequest) {
        List<OrderItem> items = createOrderRequest.items().stream()
                .map(
                        item -> new OrderItem(
                                item.productId(),
                                item.productName(),
                                item.quantity(),
                                item.price()
                        )
                )
                .toList();

        Order order = new Order(items);

        return jooqOrderRepository.save(order);
    }
}

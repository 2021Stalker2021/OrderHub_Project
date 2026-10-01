package oleborn.orderservice.order.demo;

import gsc.edward.OrderHub_Project.order.CreateOrderRequest;
import gsc.edward.OrderHub_Project.order.Order;
import gsc.edward.OrderHub_Project.order.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/demo/orders")
public class OrderDemoController {

    private final DemoOrderService demoOrderService;

    @PostMapping("/jdbc")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest
    ) {
        Order withJdbc = demoOrderService.createWithJdbc(createOrderRequest);
        OrderResponse response = OrderResponse.from(withJdbc);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/jooq")
    public ResponseEntity<OrderResponse> createOrderWithJooq(
            @Valid @RequestBody CreateOrderRequest createOrderRequest
    ) {
        Order withJdbc = demoOrderService.createWithJdbc(createOrderRequest);
        OrderResponse response = OrderResponse.from(withJdbc);

        return ResponseEntity.ok(response);
    }
}

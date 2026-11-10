package oleborn.orderservice.order.demo;

import oleborn.orderservice.order.domain.dto.CreateOrderRequestDto;
import oleborn.orderservice.order.domain.Order;
import oleborn.orderservice.order.domain.dto.OrderResponseDto;
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
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto createOrderRequestDto
    ) {
        Order withJdbc = demoOrderService.createWithJdbc(createOrderRequestDto);
        OrderResponseDto response = OrderResponseDto.from(withJdbc);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/jooq")
    public ResponseEntity<OrderResponseDto> createOrderWithJooq(
            @Valid @RequestBody CreateOrderRequestDto createOrderRequestDto
    ) {
        Order withJdbc = demoOrderService.createWithJdbc(createOrderRequestDto);
        OrderResponseDto response = OrderResponseDto.from(withJdbc);

        return ResponseEntity.ok(response);
    }
}

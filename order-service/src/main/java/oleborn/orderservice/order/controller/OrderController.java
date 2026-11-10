package oleborn.orderservice.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import oleborn.orderservice.order.domain.dto.CreateOrderRequestDto;
import oleborn.orderservice.order.domain.dto.OrderResponseDto;
import oleborn.orderservice.order.domain.Order;
import oleborn.orderservice.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto request
    ) {
        Order order = orderService.createOrder(request);
        OrderResponseDto response = OrderResponseDto.from(order);

        return ResponseEntity.created(URI.create("/orders/" + order.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderWithItems(@PathVariable Long id) {
        Order order = orderService.getOrderWithItems(id);
        OrderResponseDto response = OrderResponseDto.from(order);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/failure-mode")
    public void setFailureMode(@RequestParam boolean enabled) {
        orderService.setFailureMode(enabled);
    }
}
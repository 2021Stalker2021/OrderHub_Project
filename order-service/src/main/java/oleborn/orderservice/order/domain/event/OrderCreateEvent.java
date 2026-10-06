package oleborn.orderservice.order.domain.event;

import java.time.LocalDateTime;

public record OrderCreateEvent(
        Long orderId,
        LocalDateTime timestamp
) {
    public static OrderCreateEvent of(Long orderId) {
        return new OrderCreateEvent(orderId, LocalDateTime.now());
    }
}

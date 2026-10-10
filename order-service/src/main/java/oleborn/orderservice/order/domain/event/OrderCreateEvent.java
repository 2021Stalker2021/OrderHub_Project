package oleborn.orderservice.order.domain.event;

import java.time.LocalDateTime;
import java.util.Map;

public record OrderCreateEvent(
        Long orderId,
        Map<String, String> context,
        LocalDateTime timestamp
) {
    public static OrderCreateEvent of(Long orderId, Map<String, String> context) {
        return new OrderCreateEvent(
                orderId,
                context,
                LocalDateTime.now()
        );
    }
}

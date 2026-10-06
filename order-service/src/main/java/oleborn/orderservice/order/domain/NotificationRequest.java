package oleborn.orderservice.order.domain;

import lombok.Builder;

@Builder
public record NotificationRequest(
        Long orderId,
        String eventType
) {
}

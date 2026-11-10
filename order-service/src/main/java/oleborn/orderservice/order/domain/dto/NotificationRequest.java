package oleborn.orderservice.order.domain.dto;

import lombok.Builder;

@Builder
public record NotificationRequest(
        Long orderId,
        String eventType
) {
}

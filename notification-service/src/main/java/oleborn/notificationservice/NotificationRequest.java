package oleborn.notificationservice;

public record NotificationRequest(
        Long orderId,
        String eventType
) {
}

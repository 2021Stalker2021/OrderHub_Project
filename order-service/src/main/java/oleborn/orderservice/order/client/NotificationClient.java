package oleborn.orderservice.order.client;

import lombok.RequiredArgsConstructor;
import oleborn.orderservice.order.domain.NotificationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final WebClient notificationWebClient;

    public void notifyOrderCreate(Long orderId) {

        NotificationRequest request = NotificationRequest.builder()
                .orderId(orderId)
                .eventType("CREATED")
                .build();

        notificationWebClient.post()
                .uri("/api/notifications")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

}

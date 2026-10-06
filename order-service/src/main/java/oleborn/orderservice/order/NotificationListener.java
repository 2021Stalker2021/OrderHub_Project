package oleborn.orderservice.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oleborn.orderservice.order.client.NotificationClient;
import oleborn.orderservice.order.domain.event.OrderCreateEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationClient notificationClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    /*
     Метод внутри NotificationListener не начнет выполняться сразу в момент вызова publishEvent.
     Он «встанет на паузу» и будет ждать, пока OrderService полностью завершит свою транзакцию, а база данных
     подтвердит (Commit), что заказ успешно и без ошибок записан на диск.
     */
    public void handleOrderCreated(OrderCreateEvent event) {
        log.info("Подготовлено событие к отправке: {}", event);

        try {
            notificationClient.notifyOrderCreate(event.orderId());
            log.debug("Уведомление успешно отправлено по id: {}", event.orderId());
        } catch (Exception e) {
            log.error("Ошибка отправки события по созданию заказа id: {}", event.orderId(), e);
        }
    }
}

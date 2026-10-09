package oleborn.notificationservice;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    @PostMapping
    @SneakyThrows
    public ResponseEntity<Void> notify(@RequestBody NotificationRequest request) {

        log.info("NotificationRequest: {}", request);

        int random = new Random().nextInt(100);

        log.info("Выпало число: {}", random);

        if (random < 30) {
            log.error("Возникли проблемы с отправкой информации по заказу: {}", request.orderId());
            throw new RuntimeException("Типа проблемы с отправкой уведомления");
        }

        if (random > 70) {
            log.warn("NotificationService замедлился");
            Thread.sleep(200);
        }

        return ResponseEntity.ok().build();
    }
}

package oleborn.notificationservice;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    @PostMapping
    @SneakyThrows
    public ResponseEntity<Void> notify(@RequestBody NotificationRequest notificationRequest) {
        log.info("NotificationRequest: {}", notificationRequest);

        Thread.sleep(200);

        return ResponseEntity.ok().build();
    }
}

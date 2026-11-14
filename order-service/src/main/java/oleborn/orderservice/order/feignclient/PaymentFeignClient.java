package oleborn.orderservice.order.feignclient;

import oleborn.orderservice.order.config.FeignConfig;
import oleborn.orderservice.order.domain.dto.PaymentRequestDto;
import oleborn.orderservice.order.domain.dto.PaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "payment-service",
        url = "${payment-service.url:http://payment-service:8083}",
        configuration = FeignConfig.class
)
public interface PaymentFeignClient {

    @PostMapping("/api/v1/payments/process")
    PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto);
}

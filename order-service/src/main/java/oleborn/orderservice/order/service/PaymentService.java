package oleborn.orderservice.order.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oleborn.orderservice.order.client.PaymentClient;
import oleborn.orderservice.order.domain.Order;
import oleborn.orderservice.order.domain.dto.PaymentResponseDto;
import oleborn.orderservice.order.exception.PaymentFailedException;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentClient paymentClient;

    @Retryable(
            retryFor = {
                    WebClientResponseException.class,
                    PaymentFailedException.class,
            },
            noRetryFor = {
                    IllegalArgumentException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 200,
                    maxDelay = 5000,
                    multiplier = 2,
                    random = true
            )
    )
    public PaymentResponseDto processPaymentDefault(Order savedOrder) {

        RetryContext context = RetrySynchronizationManager.getContext();
        int attempt = context != null ? context.getRetryCount() + 1 : 1;

        log.debug("ПОПЫТКА {} ИЗ 3 <====", attempt);

        try {
            PaymentResponseDto responseDto = paymentClient.processPayment(
                    savedOrder.getId(),
                    savedOrder.getTotalPrice()
            ).block();

            log.info("Попытка {} успешна", attempt);

            return responseDto;
        } catch (WebClientResponseException e) {
            log.error("Попытка {}: техническая ошибка в payment-service {}",attempt, e.getMessage());
            throw new PaymentFailedException("Payment-service error: " + e.getMessage());
        }
    }


    @CircuitBreaker(
            name = "paymentService",
            fallbackMethod = "fallbackProcessPayment"
    )
    @Retry(name = "paymentService")
    public PaymentResponseDto processPaymentResilience4j(Order savedOrder) {

        try {
            PaymentResponseDto responseDto = paymentClient.processPayment(
                    savedOrder.getId(),
                    savedOrder.getTotalPrice()
            ).block();

            log.info("Попытка успешна");

            return responseDto;
        } catch (WebClientResponseException e) {
            log.error("Техническая ошибка в payment-service {}", e.getMessage());
            throw new PaymentFailedException("Payment-service error: " + e.getMessage());
        }
    }

    @Recover
    public PaymentResponseDto fallbackProcessPayment(Order savedOrder, Throwable t) {

        log.warn("Резервная логика для заказа: {}, после срабатывания CircuitBreaker: {}", savedOrder.getId(), t.getMessage());

        throw new PaymentFailedException("Payment-service error: " + t.getMessage());
    }
}

package oleborn.orderservice.order.domain.dto;

import lombok.Builder;

@Builder
public record PaymentResponseDto(

        Boolean isSuccessful,
        String message,
        boolean isRequiredPendingProcess

) {
}
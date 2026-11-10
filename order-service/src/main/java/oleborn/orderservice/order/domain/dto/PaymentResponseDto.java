package oleborn.orderservice.order.domain.dto;

public record PaymentResponseDto(

        Boolean isSuccessful,
        String message

) {
}
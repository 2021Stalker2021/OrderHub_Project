package oleborn.orderservice.order;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDto(
        int status,
        String code,
        Object details
) {
}

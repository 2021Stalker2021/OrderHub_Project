package oleborn.orderservice.order.exception;

import oleborn.orderservice.order.domain.dto.ErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleException(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ":" + error.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest().body(new ErrorDto(ex.getStatusCode().value(),"Validation Failed", errors));
    }

    @ExceptionHandler(NotFoundOrderException.class)
    public ResponseEntity<ErrorDto> handleException(NotFoundOrderException ex) {

        ErrorDto error = new ErrorDto(404, "Order Not Found", ex.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorDto> handleException(PaymentFailedException ex) {

        ErrorDto error = new ErrorDto(500, "Ошибка приложения", ex.getMessage());

        return ResponseEntity.internalServerError().body(error);

    }

    @ExceptionHandler(CircuitBreakerOpenException.class)
    public ResponseEntity<ErrorDto> handleException(CircuitBreakerOpenException ex) {

        ErrorDto error = new ErrorDto(500, "Ошибка приложения", ex.getMessage());

        return ResponseEntity.internalServerError().body(error);

    }
}

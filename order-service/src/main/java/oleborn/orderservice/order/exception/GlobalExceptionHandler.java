package oleborn.orderservice.order.exception;

import oleborn.orderservice.order.domain.ErrorDto;
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

        ErrorDto orderNotFound = new ErrorDto(404, "Order Not Found", ex.getMessage());

        return ResponseEntity.badRequest().body(orderNotFound);
    }
}

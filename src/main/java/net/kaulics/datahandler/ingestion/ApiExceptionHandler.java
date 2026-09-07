package net.kaulics.datahandler.ingestion;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import net.kaulics.datahandler.api.model.ErrorResponse;
import net.kaulics.datahandler.exception.UnknownDeviceTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.DatabindException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UnknownDeviceTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnknownDeviceType(UnknownDeviceTypeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(List.of(ex.getMessage())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ConstraintViolationException ex) {
        List<String> messages = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(messages));
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class, DatabindException.class})
    public ResponseEntity<ErrorResponse> handleMalformedPayload(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(List.of("Malformed payload: " + ex.getMessage())));
    }
}

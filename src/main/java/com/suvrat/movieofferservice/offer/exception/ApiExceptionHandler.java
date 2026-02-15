package com.suvrat.movieofferservice.offer.exception;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(OfferValidationException.class)
    public ResponseEntity<Map<String, Object>> handleOfferValidation(OfferValidationException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "error", "VALIDATION_ERROR",
                "message", exception.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnhandled(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "error", "INTERNAL_ERROR",
                "message", exception.getMessage()
        ));
    }
}

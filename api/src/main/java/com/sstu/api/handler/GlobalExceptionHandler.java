package com.sstu.api.handler;

import com.sstu.api.circuit.CircuitBreakerOpenException;
import com.sstu.api.exception.ServerErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<String> handleServerErrorException(ServerErrorException ex) {
        return ResponseEntity.status(500).body("External server error");
    }
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> handleResourceAccessException(ResourceAccessException ex) {
        return ResponseEntity.status(500).body("External server error");
    }
    @ExceptionHandler(CircuitBreakerOpenException.class)
    public ResponseEntity<String> handleOpenException(CircuitBreakerOpenException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }
}

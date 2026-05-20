package com.sstu.api.circuit;

public class CircuitBreakerOpenException extends RuntimeException {
  public CircuitBreakerOpenException(String message) {
    super(message);
  }
}

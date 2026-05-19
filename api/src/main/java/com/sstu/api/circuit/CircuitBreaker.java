package com.sstu.api.circuit;

import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CircuitBreaker {

    private final int failureThreshold;
    private final long timeoutMs;
    private final int successThreshold;
    private final int maxRetries;

    public CircuitBreaker(int failureThreshold, long timeoutMs, int successThreshold, int maxRetries) {
        this.failureThreshold = failureThreshold;
        this.timeoutMs = timeoutMs;
        this.successThreshold = successThreshold;
        this.maxRetries = maxRetries;
    }

    private int consecutiveFailuresInClosedState;
    private int consecutiveSuccessesInHalfOpenState;
    private long openedAtTimestamp;
    private State state = State.CLOSED;

    private final Object lock = new Object();

    public <T> T execute(Supplier<T> supplier) {

        synchronized (lock) {
            checkAndTransitionFromOpen();

            if (state == State.OPEN) {
                throw new OpenException("CircuitBreaker OPEN");
            }

            return executeWithRetry(supplier, maxRetries);
        }

    }

    private <T> T executeWithRetry(Supplier<T> supplier, int retries) {
        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            if (retries > 0) {
                log.warn("Ошибка при выполнении запроса. Осталось попыток: {}. Повторяем...", retries);
                return executeWithRetry(supplier, retries - 1);
            } else {
                log.error("Все попытки ({}) исчерпаны. Вызываем onFailure()", maxRetries);
                onFailure();
                throw e;
            }
        }
    }

    private void onFailure() {

        synchronized (lock) {
            if (state == State.HALF_OPEN) {
                log.warn("Ошибка в HALF_OPEN → переходим в OPEN");
                transition(State.OPEN);
                openedAtTimestamp = System.currentTimeMillis();
                return;
            }

            consecutiveFailuresInClosedState++;
            log.warn("Ошибка {}/{}", consecutiveFailuresInClosedState, failureThreshold);

            if (consecutiveFailuresInClosedState >= failureThreshold) {
                log.warn("Достигнут порог ошибок → переходим в OPEN на {} мс", timeoutMs);
                transition(State.OPEN);
                openedAtTimestamp = System.currentTimeMillis();
            }
        }
    }

    private void onSuccess() {
        synchronized (lock) {

            if (state == State.HALF_OPEN) {
                consecutiveSuccessesInHalfOpenState++;
                log.info("Успех {}/{} в HALF_OPEN", consecutiveSuccessesInHalfOpenState, successThreshold);

                if (consecutiveSuccessesInHalfOpenState >= successThreshold) {
                    log.info("Достигнут порог успехов → переходим в CLOSED");
                    transition(State.CLOSED);
                    reset();
                }

                return;
            }
            if (state == State.CLOSED) {
                consecutiveFailuresInClosedState = 0;
                log.debug("Успех, счетчик ошибок сброшен");
            }
        }
    }

    private void reset() {
        consecutiveSuccessesInHalfOpenState = 0;
        consecutiveFailuresInClosedState = 0;
    }

    private void transition(State newState) {
        State prev = this.state;

        if (prev != newState) {
            this.state = newState;
            log.info("{} → {}", prev, newState);
        }
    }

    private void checkAndTransitionFromOpen() {
        if (state == State.OPEN) {
            long openTime = System.currentTimeMillis() - openedAtTimestamp;

            if (openTime >= timeoutMs) {
                log.info("Таймаут прошел → переходим в HALF_OPEN");
                transition(State.HALF_OPEN);
                consecutiveSuccessesInHalfOpenState = 0;
            } else {
                long remaining = timeoutMs - openTime;
                throw new OpenException("CircuitBreaker OPEN, повторите через " + remaining + " мс");
            }
        }
    }
}
package kz.iitu.springlab.aspect;

import java.time.Instant;

public record SlowCall(
        String method,
        long executionTimeMs,
        long thresholdMs,
        Instant recordedAt
) {
}

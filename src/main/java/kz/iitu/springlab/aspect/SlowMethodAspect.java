package kz.iitu.springlab.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@Order(4)
public class SlowMethodAspect {

    private final SlowCallRegistry registry;
    private final long slowThresholdMs;

    public SlowMethodAspect(
            SlowCallRegistry registry,
            @Value("${app.aspect.slow-threshold-ms:200}") long slowThresholdMs) {
        this.registry = registry;
        this.slowThresholdMs = slowThresholdMs;
    }

    @Around("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public Object recordSlowCall(ProceedingJoinPoint pjp) throws Throwable {
        long started = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long ms = (System.nanoTime() - started) / 1_000_000;
            if (ms > slowThresholdMs) {
                registry.record(new SlowCall(
                        pjp.getSignature().toShortString(), ms, slowThresholdMs, Instant.now()));
            }
        }
    }
}

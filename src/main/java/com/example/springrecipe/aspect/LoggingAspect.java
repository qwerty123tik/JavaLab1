package com.example.springrecipe.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    private static final long SLOW_THRESHOLD_MS = 300;
    private static final long VERY_SLOW_THRESHOLD_MS = 1000;

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            logExecutionTime(methodName, executionTime);
            return result;
        } catch (Throwable ex) {
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            log.warn("{} failed after {} ms: {}", methodName, executionTime, ex.getMessage());
            throw ex;
        }
    }

    private void logExecutionTime(String methodName, long executionTime) {
        if (executionTime >= VERY_SLOW_THRESHOLD_MS) {
            log.warn("{} executed in {} ms (VERY SLOW)", methodName, executionTime);
        } else if (executionTime >= SLOW_THRESHOLD_MS) {
            log.info("{} executed in {} ms", methodName, executionTime);
        } else {
            log.debug("{} executed in {} ms", methodName, executionTime);
        }
    }
}

package com.example.springrecipe.service;

import com.example.springrecipe.dto.RaceConditionDemoDTO;
import com.example.springrecipe.exceptions.RaceConditionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RaceConditionDemoService {
    private int unsafeCounter = 0;
    private final AtomicInteger safeCounter = new AtomicInteger(0);

    public void incrementUnsafe() {
        unsafeCounter++;
    }

    public void incrementSafe() {
        safeCounter.incrementAndGet();
    }

    public void resetCounters() {
        unsafeCounter = 0;
        safeCounter.set(0);
    }

    public int getUnsafeCounter() {
        return unsafeCounter;
    }

    public int getSafeCounter() {
        return safeCounter.get();
    }

    public RaceConditionDemoDTO demonstrateRaceCondition() {
        return runTest(false);
    }

    public RaceConditionDemoDTO demonstrateSolution() {
        return runTest(true);
    }

    private RaceConditionDemoDTO runTest(boolean useSafe) {

        resetCounters();

        int threadCount = 50;
        int incrementsPerThread = 1000;
        int expectedTotal = threadCount * incrementsPerThread;
        String mode = useSafe ? "atomic" : "unsafe";

        log.info("=== {} ===", useSafe ? "AtomicInteger" : "Race condition");
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    if (useSafe) {
                        incrementSafe();
                    } else {
                        incrementUnsafe();
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RaceConditionException("Поток был прерван", e);
        }

        long endTime = System.currentTimeMillis();
        long actual = useSafe ? getSafeCounter() : getUnsafeCounter();
        long lost = expectedTotal - actual;
        long durationMs = endTime - startTime;

        log.info("Результат: {} (ожидалось: {}), потеряно: {}, время: {} ms",
                actual, expectedTotal, lost, durationMs);

        return new RaceConditionDemoDTO(mode, threadCount, incrementsPerThread,
                expectedTotal, actual, lost, durationMs);
    }
}

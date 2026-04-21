package com.example.springrecipe.service;
import com.example.springrecipe.dto.RaceConditionDemoDTO;
import com.example.springrecipe.exceptions.RaceConditionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
class RaceConditionDemoServiceTest {

    private RaceConditionDemoService service;

    @BeforeEach
    void setUp() {
        service = new RaceConditionDemoService();
    }

    @Test
    void incrementUnsafe_shouldIncreaseCounter() {
        service.incrementUnsafe();
        service.incrementUnsafe();
        assertThat(service.getUnsafeCounter()).isEqualTo(2);
    }

    @Test
    void incrementSafe_shouldIncreaseCounter() {
        service.incrementSafe();
        service.incrementSafe();
        assertThat(service.getSafeCounter()).isEqualTo(2);
    }

    @Test
    void resetCounters_shouldResetCounters() {
        service.incrementUnsafe();
        service.incrementSafe();
        service.resetCounters();
        assertThat(service.getUnsafeCounter()).isZero();
        assertThat(service.getSafeCounter()).isZero();
    }

    @Test
    void demonstrateSolution_shouldWorkCorrectly() {
        RaceConditionDemoDTO result = service.demonstrateSolution();
        assertThat(result).isNotNull();
        assertThat(result.getMode()).isEqualTo("atomic");
        assertThat(result.getExpected())
                .isEqualTo(result.getThreadCount() * result.getIncrementsPerThread());
        assertThat(result.getActual()).isEqualTo(result.getExpected());
        assertThat(result.getLost()).isZero();
    }

    @RepeatedTest(5)
    void demonstrateRaceCondition_shouldLoseIncrements() {
        RaceConditionDemoDTO result = service.demonstrateRaceCondition();
        assertThat(result).isNotNull();
        assertThat(result.getMode()).isEqualTo("unsafe");
        assertThat(result.getExpected())
                .isEqualTo(result.getThreadCount() * result.getIncrementsPerThread());
        assertThat(result.getActual()).isLessThanOrEqualTo(result.getExpected());
    }

@Test
void runTest_shouldThrowRaceConditionException_whenInterrupted() throws InterruptedException {
    java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
    Thread t = new Thread(() -> {
        try {
            latch.countDown();
            service.demonstrateSolution();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
    t.start();
    latch.await();
    t.interrupt();
    t.join();
    assertThatThrownBy(() -> t.join()).hasCauseInstanceOf(RaceConditionException.class);
}
}
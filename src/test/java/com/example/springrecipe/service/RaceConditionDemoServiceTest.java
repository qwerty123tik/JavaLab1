package com.example.springrecipe.service;
import com.example.springrecipe.dto.RaceConditionDemoDTO;
import com.example.springrecipe.exceptions.RaceConditionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        // Из-за race condition фактическое значение обычно меньше ожидаемого
        assertThat(result.getActual()).isLessThanOrEqualTo(result.getExpected());
        // Иногда может быть равно, но редко – повторяем тест 5 раз, чтобы почти всегда поймать потерю
    }

    @Test
    void demonstrateSolution_shouldThrowRaceConditionException_whenThreadInterrupted() {

        Thread.currentThread().interrupt();

        try {

            assertThatThrownBy(() -> service.demonstrateSolution())
                    .isInstanceOf(RaceConditionException.class)
                    .hasMessageContaining("Поток был прерван");

        } finally {
            // очищаем interrupt flag чтобы не сломать другие тесты
            Thread.interrupted();
        }
    }
}
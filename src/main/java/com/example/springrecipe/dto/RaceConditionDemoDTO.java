package com.example.springrecipe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaceConditionDemoDTO {
    private String mode;
    private int threadCount;
    private int incrementsPerThread;
    private long expected;
    private long actual;
    private long lost;
    private long durationMs;
}

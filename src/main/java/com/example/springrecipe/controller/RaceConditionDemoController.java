package com.example.springrecipe.controller;

import com.example.springrecipe.dto.RaceConditionDemoDTO;
import com.example.springrecipe.service.RaceConditionDemoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/race-condition")
@AllArgsConstructor
public class RaceConditionDemoController {

    private final RaceConditionDemoService raceConditionDemoService;

    @GetMapping("/unsafe")
    public RaceConditionDemoDTO demoUnsafe() {
        return raceConditionDemoService.demonstrateRaceCondition();
    }

    @GetMapping("/safe")
    public RaceConditionDemoDTO demoSafe() {
        return raceConditionDemoService.demonstrateSolution();
    }
}

package com.example.springrecipe.controller;

import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/start")
    public ResponseEntity<String> startTask(@RequestBody List<RecipeDTO> recipes) {
        String taskId = taskService.startAsyncProcessing(recipes);
        return ResponseEntity.accepted().body(taskId);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<String> getStatus(@PathVariable String id) {
        String status = taskService.getStatus(id);
        return ResponseEntity.ok(status);
    }
}

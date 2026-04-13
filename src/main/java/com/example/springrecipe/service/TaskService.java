package com.example.springrecipe.service;

import com.example.springrecipe.dto.RecipeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final RecipeService recipeService;
    private final Map<String, String> statusMap = new ConcurrentHashMap<>();

    public String startAsyncProcessing(List<RecipeDTO> recipes) {
        String taskId = UUID.randomUUID().toString();
        statusMap.put(taskId, "IN_PROGRESS");
        CompletableFuture.runAsync(() -> processRecipes(taskId, recipes));
        return taskId;
    }

    @Async("taskExecutor")
    public void processRecipes(String taskId, List<RecipeDTO> recipes) {
        try {
            for (RecipeDTO dto : recipes) {
                if (dto.getName() == null || dto.getName().isBlank()) {
                    throw new IllegalArgumentException("Рецепт с пустым именем (FAILED)");
                }
                recipeService.createRecipe(dto);
            }
            statusMap.put(taskId, "DONE");
            log.info("Задача {} завершена успешно", taskId);
        } catch (Exception e) {
            statusMap.put(taskId, "FAILED");
            log.error("Задача {} завершена с ошибкой: {}", taskId, e.getMessage());
        }
    }

    public String getStatus(String taskId) {
        return statusMap.getOrDefault(taskId, "NOT_FOUND");
    }
}

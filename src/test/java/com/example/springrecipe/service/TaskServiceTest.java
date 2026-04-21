package com.example.springrecipe.service;

import com.example.springrecipe.dto.RecipeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private TaskService taskService;

    private List<RecipeDTO> testRecipes;
    private RecipeDTO testRecipe;

    @BeforeEach
    void setUp() {
        testRecipe = new RecipeDTO();
        testRecipe.setName("Тестовый рецепт");
        testRecipe.setCookingTime(30);
        testRecipe.setAuthorId(1L);
        testRecipe.setCategoryId(1L);

        testRecipes = List.of(testRecipe);
    }

    @Test
     void startAsyncProcessing_shouldReturnTaskIdAndSetStatus() {
        String taskId = taskService.startAsyncProcessing(testRecipes);

        assertThat(taskId)
                .isNotNull()
                .isNotEmpty();

        assertThat(taskService.getStatus(taskId))
                .isEqualTo("IN_PROGRESS");
    }

    @Test
    void processRecipes_success_shouldSetStatusDone() throws Exception {
        String taskId = UUID.randomUUID().toString();
        taskService.getStatus(taskId);
        taskService.getStatus(taskId);
        java.lang.reflect.Field field = TaskService.class.getDeclaredField("statusMap");
        field.setAccessible(true);
        Map<String, String> statusMap = (Map<String, String>) field.get(taskService);
        statusMap.put(taskId, "IN_PROGRESS");

        taskService.processRecipes(taskId, testRecipes);

        assertThat(statusMap)
                .containsEntry(taskId, "DONE");
        verify(recipeService, times(testRecipes.size())).createRecipe(any(RecipeDTO.class));
    }

    @Test
    void processRecipes_failure_shouldSetStatusFailed() throws Exception {
        String taskId = UUID.randomUUID().toString();
        doThrow(new RuntimeException("Ошибка БД")).when(recipeService).createRecipe(any(RecipeDTO.class));

        java.lang.reflect.Field field = TaskService.class.getDeclaredField("statusMap");
        field.setAccessible(true);
        Map<String, String> statusMap = (Map<String, String>) field.get(taskService);
        statusMap.put(taskId, "IN_PROGRESS");

        taskService.processRecipes(taskId, testRecipes);

        assertThat(statusMap)
                .containsEntry(taskId, "FAILED");
        verify(recipeService, times(1)).createRecipe(any(RecipeDTO.class));
    }

    @Test
    void getStatus_existingTaskId_shouldReturnStatus() {
        String taskId = taskService.startAsyncProcessing(testRecipes);

        String status = taskService.getStatus(taskId);

        assertThat(status).isEqualTo("IN_PROGRESS");
    }

    @Test
    void getStatus_nonExistingTaskId_shouldReturnNotFound() {
        String status = taskService.getStatus("non-existing-id");

        assertThat(status).isEqualTo("NOT_FOUND");
    }

    @Test
    void startAsyncProcessing_shouldTriggerAsyncProcessing() {
        String taskId = taskService.startAsyncProcessing(testRecipes);
        assertThat(taskId).isNotNull();

        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> taskService.getStatus(taskId).equals("DONE"));
    }

    @Test
    void processRecipes_withEmptyName_shouldSetStatusFailed() {
        RecipeDTO emptyNameRecipe = new RecipeDTO();
        emptyNameRecipe.setName("");
        emptyNameRecipe.setCookingTime(30);
        emptyNameRecipe.setAuthorId(1L);
        emptyNameRecipe.setCategoryId(1L);
        List<RecipeDTO> recipes = List.of(emptyNameRecipe);

        String taskId = taskService.startAsyncProcessing(recipes);

        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> taskService.getStatus(taskId).equals("FAILED"));

        verify(recipeService, never()).createRecipe(any(RecipeDTO.class));
    }

    @Test
    void processRecipes_withNullName_shouldSetStatusFailed() {
        RecipeDTO nullNameRecipe = new RecipeDTO();
        nullNameRecipe.setName(null);
        nullNameRecipe.setCookingTime(30);
        nullNameRecipe.setAuthorId(1L);
        nullNameRecipe.setCategoryId(1L);
        List<RecipeDTO> recipes = List.of(nullNameRecipe);

        String taskId = taskService.startAsyncProcessing(recipes);

        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> taskService.getStatus(taskId).equals("FAILED"));

        verify(recipeService, never()).createRecipe(any(RecipeDTO.class));
    }
}

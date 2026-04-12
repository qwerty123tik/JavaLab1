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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
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

        assertThat(taskId).isNotNull();
        assertThat(taskId).isNotEmpty();
        assertThat(taskService.getStatus(taskId)).isEqualTo("IN_PROGRESS");
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

        assertThat(statusMap.get(taskId)).isEqualTo("DONE");
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

        assertThat(statusMap.get(taskId)).isEqualTo("FAILED");
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
    void startAsyncProcessing_shouldTriggerAsyncProcessing() throws Exception {
        String taskId = taskService.startAsyncProcessing(testRecipes);

        assertThat(taskId).isNotNull();
        assertThat(taskService.getStatus(taskId)).isEqualTo("IN_PROGRESS");
    }
}

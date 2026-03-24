package com.example.springrecipe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long id;

    @NotNull(message = "Оценка обязательна")
    @Min(value = 1, message = "Минимальная оценка — 1")
    @Max(value = 5, message = "Максимальная оценка — 5")
    private Integer rating;

    @Size(max = 1000, message = "Комментарий слишком длинный")
    private String comment;

    @NotNull(message = "ID пользователя обязателен")
    private Long userId;
    private String userName;

    @NotNull(message = "ID рецепта обязателен")
    private Long recipeId;
    private String recipeName;
}

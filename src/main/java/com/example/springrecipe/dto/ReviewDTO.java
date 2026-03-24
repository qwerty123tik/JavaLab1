package com.example.springrecipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "DTO для отзыва")
public class ReviewDTO {
    @Schema(description = "ID отзыва", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Оценка обязательна")
    @Min(value = 1, message = "Минимальная оценка — 1")
    @Max(value = 5, message = "Максимальная оценка — 5")
    @Schema(description = "Оценка рецепта", example = "5", minimum = "1", maximum = "5",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer rating;

    @Size(max = 1000, message = "Комментарий слишком длинный")
    @Schema(description = "Текст отзыва", example = "Отличный рецепт!")
    private String comment;

    @NotNull(message = "ID пользователя обязателен")
    @Schema(description = "ID автора отзыва", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(description = "Имя пользователя", example = "TikiTaka", accessMode = Schema.AccessMode.READ_ONLY)
    private String userName;

    @NotNull(message = "ID рецепта обязателен")
    @Schema(description = "ID рецепта", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recipeId;

    @Schema(description = "Название рецепта", example = "Шоколадный торт", accessMode = Schema.AccessMode.READ_ONLY)
    private String recipeName;
}

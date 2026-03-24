package com.example.springrecipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для рецепта")
public class RecipeDTO {
    @Schema(description = "ID рецепта", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Название рецепта не может быть пустым")
    @Size(min = 3, max = 100, message = "Название должно быть от 3 до 100 символов")
    @Schema(description = "Название рецепта", example = "Шоколадный торт", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    @Schema(description = "Описание рецепта", example = "Нежный шоколадный торт с кремом")
    private String description;

    @NotNull(message = "Укажите время приготовления")
    @Min(value = 1, message = "Время приготовления должно быть не менее 1 минуты")
    @Schema(description = "Время приготовления в минутах", example = "60", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cookingTime;

    @Schema(description = "Название категории", example = "Десерты", accessMode = Schema.AccessMode.READ_ONLY)
    private String categoryName;
    @Schema(description = "Имя автора", example = "TikiTaka", accessMode = Schema.AccessMode.READ_ONLY)
    private String authorName;
    @Schema(description = "Средний рейтинг рецепта", example = "4.5", accessMode = Schema.AccessMode.READ_ONLY)
    private Double averageRating;

    @NotNull(message = "ID категории обязателен")
    @Schema(description = "ID категории рецепта", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @NotNull(message = "ID автора обязателен")
    @Schema(description = "ID автора рецепта", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long authorId;

    @NotEmpty(message = "Рецепт должен содержать хотя бы один ингредиент")
    @Schema(description = "Список ингредиентов с количеством", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RecipeIngredientDTO> recipeIngredients;

    @Schema(description = "Список ID ингредиентов", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Long> ingredientIds;

    @Schema(description = "Список названий ингредиентов", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> ingredientNames;
}

package com.example.springrecipe.dto;

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
public class RecipeDTO {
    private Long id;

    @NotBlank(message = "Название рецепта не может быть пустым")
    @Size(min = 3, max = 100, message = "Название должно быть от 3 до 100 символов")
    private String name;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @NotNull(message = "Укажите время приготовления")
    @Min(value = 1, message = "Время приготовления должно быть не менее 1 минуты")
    private Integer cookingTime;

    private String categoryName;
    private String authorName;
    private Double averageRating;

    @NotNull(message = "ID категории обязателен")
    private Long categoryId;
    @NotNull(message = "ID автора обязателен")
    private Long authorId;

    @NotEmpty(message = "Рецепт должен содержать хотя бы один ингредиент")
    private List<RecipeIngredientDTO> recipeIngredients;

    private List<Long> ingredientIds;
    private List<String> ingredientNames;
}

package com.example.springrecipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для ингредиента в рецепте")
public class RecipeIngredientDTO {
    @Schema(description = "ID ингредиента", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long ingredientId;

    @NotBlank(message = "Название ингредиента обязательно")
    @Schema(description = "Название ингредиента", example = "Мука", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ingredientName;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть не менее 1")
    @Schema(description = "Количество ингредиента", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double quantity;

    @Schema(description = "Аббревиация единицы измерения", example = "г")
    private String unitAbbreviation;

    @Schema(description = "Название единицы измерения", example = "грамм", accessMode = Schema.AccessMode.READ_ONLY)
    private String unitName;
}

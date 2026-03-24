package com.example.springrecipe.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientDTO {
    private Long ingredientId;

    @NotBlank(message = "Название ингредиента обязательно")
    private String ingredientName;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть не менее 1")
    private Double quantity;

    private String unitAbbreviation;
    private String unitName;
}

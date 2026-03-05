package com.example.springrecipe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredientDTO {
    private Long ingredientId;
    private String ingredientName;
    private Double quantity;
    private String unitAbbreviation;
    private String unitName;
}

package com.example.springrecipe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    private Long id;
    private String name;
    private String description;
    private Integer cookingTime;

    private String categoryName;
    private String authorName;
    private List<String> ingredientNames;
    private Double averageRating;

    private Long categoryId;
    private Long authorId;
    private List<Long> ingredientIds;
    private List<IngredientDTO> ingredients;
}

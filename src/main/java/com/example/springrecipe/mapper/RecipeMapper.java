package com.example.springrecipe.mapper;

import com.example.springrecipe.dto.RecipeRequest;
import com.example.springrecipe.dto.RecipeResponse;
import com.example.springrecipe.model.Recipe;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper {

    public RecipeResponse toResponse(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        RecipeResponse dto = new RecipeResponse();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setInstructions(recipe.getInstructions());
        dto.setCookingTime(recipe.getCookingTime());
        dto.setDifficulty(recipe.getDifficulty());
        return dto;
    }

    public Recipe toEntity(RecipeRequest dto) {
        if (dto == null) {
            return null;
        }

        Recipe recipe = new Recipe();
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setInstructions(dto.getInstructions());
        recipe.setCookingTime(dto.getCookingTime());
        recipe.setDifficulty(dto.getDifficulty());
        return recipe;
    }
}

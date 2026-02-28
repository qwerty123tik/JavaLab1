package com.example.springrecipe.service;

import com.example.springrecipe.dto.RecipeRequest;
import com.example.springrecipe.dto.RecipeResponse;

import java.util.List;

public interface RecipeService {

    List<RecipeResponse> getAllRecipes();

    RecipeResponse getRecipeById(Long id);

    List<RecipeResponse> getRecipesByDifficulty(String difficulty);

    RecipeResponse createRecipe(RecipeRequest recipeRequest);

    RecipeResponse updateRecipe(Long id, RecipeRequest recipeRequest);

    void deleteRecipe(Long id);

}

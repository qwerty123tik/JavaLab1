package com.example.springrecipe.service.impl;

import com.example.springrecipe.dto.RecipeRequest;
import com.example.springrecipe.dto.RecipeResponse;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Recipe;
import com.example.springrecipe.repository.RecipeRepository;
import com.example.springrecipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;

    @Override
    public List<RecipeResponse> getAllRecipes() {
        return recipeRepository.findAll()
                .stream()
                .map(recipeMapper::toResponse)
                .toList();
    }

    @Override
    public RecipeResponse getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));
        return recipeMapper.toResponse(recipe);
    }

    @Override
    public List<RecipeResponse> getRecipesByDifficulty(String difficulty) {
        return recipeRepository.findByDifficulty(difficulty)
                .stream()
                .map(recipeMapper::toResponse)
                .toList();
    }

    @Override
    public List<RecipeResponse> getRecipesByMaxCookingTime(int maxTime) {
        return recipeRepository.findByMaxCookingTime(maxTime).stream()
                .map(recipeMapper::toResponse)
                .toList();
    }

    @Override
    public RecipeResponse createRecipe(RecipeRequest recipeRequest) {
        Recipe recipe = recipeMapper.toEntity(recipeRequest);
        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeMapper.toResponse(savedRecipe);
    }

    @Override
    public RecipeResponse updateRecipe(Long id, RecipeRequest recipeRequest) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));

        existingRecipe.setName(recipeRequest.getName());
        existingRecipe.setDescription(recipeRequest.getDescription());
        existingRecipe.setCookingTime(recipeRequest.getCookingTime());
        existingRecipe.setInstructions(recipeRequest.getInstructions());
        existingRecipe.setDifficulty(recipeRequest.getDifficulty());

        Recipe updatedRecipe = recipeRepository.save(existingRecipe);
        return recipeMapper.toResponse(updatedRecipe);
    }

    @Override
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }
}

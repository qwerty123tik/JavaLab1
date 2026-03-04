package com.example.springrecipe.service;

import com.example.springrecipe.dto.IngredientDTO;
import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.exceptions.CategoryNotFoundException;
import com.example.springrecipe.exceptions.RecipeNotFoundException;
import com.example.springrecipe.exceptions.UnitNotFoundException;
import com.example.springrecipe.exceptions.UserNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Category;
import com.example.springrecipe.model.Ingredient;
import com.example.springrecipe.model.Recipe;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.model.User;
import com.example.springrecipe.repository.CategoryRepository;
import com.example.springrecipe.repository.IngredientRepository;
import com.example.springrecipe.repository.RecipeRepository;
import com.example.springrecipe.repository.UnitRepository;
import com.example.springrecipe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final UnitRepository unitRepository;
    private final RecipeMapper mapper;

    @Transactional(readOnly = true)
    public List<RecipeDTO> getAllRecipes() {
        return recipeRepository.findAllWithDetails().stream()
                .map(mapper::toRecipeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found with id: " + id));
        return mapper.toRecipeDTO(recipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecipesByAuthor(Long authorId) {
        return recipeRepository.findByAuthorId(authorId).stream()
                .map(mapper::toRecipeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecipesByCategory(Long categoryId) {
        return recipeRepository.findByCategoryId(categoryId).stream()
                .map(mapper::toRecipeDTO)
                .toList();
    }

    @Transactional
    public RecipeDTO createRecipe(RecipeDTO dto) {
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException("Author not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Recipe recipe = new Recipe();
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setCookingTime(dto.getCookingTime());
        recipe.setAuthor(author);
        recipe.setCategory(category);

        setRecipeIngredients(recipe, dto.getIngredients());

        recipe = recipeRepository.save(recipe);
        return mapper.toRecipeDTO(recipe);
    }

    @Transactional
    public RecipeDTO updateRecipe(Long id, RecipeDTO dto) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));

        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setCookingTime(dto.getCookingTime());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
            recipe.setCategory(category);
        }

        setRecipeIngredients(recipe, dto.getIngredients());

        recipe = recipeRepository.save(recipe);
        return mapper.toRecipeDTO(recipe);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new RecipeNotFoundException("Recipe not found");
        }
        recipeRepository.deleteById(id);
    }

    private void setRecipeIngredients(Recipe recipe, List<IngredientDTO> ingredientDTOs) {
        if (ingredientDTOs == null || ingredientDTOs.isEmpty()) {
            return;
        }

        List<Ingredient> ingredients = new ArrayList<>();

        for (IngredientDTO ingDto : ingredientDTOs) {
            Ingredient ingredient = ingredientRepository.findByName(ingDto.getName())
                    .orElseGet(() -> createNewIngredient(ingDto));
            ingredients.add(ingredient);
        }

        recipe.setIngredients(ingredients);
    }

    private Ingredient createNewIngredient(IngredientDTO ingDto) {
        Ingredient newIngredient = new Ingredient();
        newIngredient.setName(ingDto.getName());

        if (ingDto.getUnitAbbreviation() != null) {
            UnitOfMeasure unit = unitRepository.findByAbbreviation(ingDto.getUnitAbbreviation())
                    .orElseThrow(() -> new UnitNotFoundException(
                            "Unit not found: " + ingDto.getUnitAbbreviation()));
            newIngredient.setUnit(unit);
        }

        return ingredientRepository.save(newIngredient);
    }
}

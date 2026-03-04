package com.example.springrecipe.service;

import com.example.springrecipe.dto.IngredientDTO;
import com.example.springrecipe.dto.RecipeDTO;
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
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));
        return mapper.toRecipeDTO(recipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecipesByAuthor(Long authorId) {
        return recipeRepository.findByAuthorId(authorId).stream()
                .map(mapper::toRecipeDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecipesByCategory(Long categoryId) {
        return recipeRepository.findByCategoryId(categoryId).stream()
                .map(mapper::toRecipeDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecipeDTO createRecipe(RecipeDTO dto) {
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Recipe recipe = new Recipe();
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setCookingTime(dto.getCookingTime());
        recipe.setAuthor(author);
        recipe.setCategory(category);

        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            List<Ingredient> ingredients = new ArrayList<>();

            for (IngredientDTO ingDto : dto.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findByName(ingDto.getName())
                        .orElseGet(() -> {
                            Ingredient newIngredient = new Ingredient();
                            newIngredient.setName(ingDto.getName());

                            if (ingDto.getUnitAbbreviation() != null) {
                                UnitOfMeasure unit = unitRepository.findByAbbreviation(ingDto.getUnitAbbreviation())
                                        .orElseThrow(() -> new RuntimeException(
                                                "Unit not found: " + ingDto.getUnitAbbreviation()));
                                newIngredient.setUnit(unit);
                            }

                            return ingredientRepository.save(newIngredient);
                        });

                ingredients.add(ingredient);
            }

            recipe.setIngredients(ingredients);
        }

        recipe = recipeRepository.save(recipe);
        return mapper.toRecipeDTO(recipe);
    }

    @Transactional
    public RecipeDTO updateRecipe(Long id, RecipeDTO dto) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setCookingTime(dto.getCookingTime());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            recipe.setCategory(category);
        }

        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            List<Ingredient> ingredients = new ArrayList<>();

            for (IngredientDTO ingDto : dto.getIngredients()) {
                Ingredient ingredient = ingredientRepository.findByName(ingDto.getName())
                        .orElseGet(() -> {
                            Ingredient newIngredient = new Ingredient();
                            newIngredient.setName(ingDto.getName());

                            if (ingDto.getUnitAbbreviation() != null) {
                                UnitOfMeasure unit = unitRepository.findByAbbreviation(ingDto.getUnitAbbreviation())
                                        .orElseThrow(() -> new RuntimeException(
                                                "Unit not found: " + ingDto.getUnitAbbreviation()));
                                newIngredient.setUnit(unit);
                            }

                            return ingredientRepository.save(newIngredient);
                        });

                ingredients.add(ingredient);
            }

            recipe.setIngredients(ingredients);
        }

        recipe = recipeRepository.save(recipe);
        return mapper.toRecipeDTO(recipe);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new RuntimeException("Recipe not found");
        }
        recipeRepository.deleteById(id);
    }

}

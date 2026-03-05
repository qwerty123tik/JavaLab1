package com.example.springrecipe.service;

import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.dto.RecipeIngredientDTO;
import com.example.springrecipe.exceptions.CategoryNotFoundException;
import com.example.springrecipe.exceptions.IngredientNotFoundException;
import com.example.springrecipe.exceptions.RecipeNotFoundException;
import com.example.springrecipe.exceptions.UnitNotFoundException;
import com.example.springrecipe.exceptions.UserNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Category;
import com.example.springrecipe.model.Ingredient;
import com.example.springrecipe.model.Recipe;
import com.example.springrecipe.model.RecipeIngredient;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.model.User;
import com.example.springrecipe.repository.CategoryRepository;
import com.example.springrecipe.repository.IngredientRepository;
import com.example.springrecipe.repository.RecipeIngredientRepository;
import com.example.springrecipe.repository.RecipeRepository;
import com.example.springrecipe.repository.UnitRepository;
import com.example.springrecipe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final UnitRepository unitRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeMapper mapper;

    @Transactional(readOnly = true)
    public List<RecipeDTO> getAllRecipesWithNPlusOneProblem() {
        return recipeRepository.findAll()
                .stream()
                .map(mapper::toRecipeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getAllRecipesWithEntityGraph() {
        return recipeRepository.findAllWithDetails()
                .stream()
                .map(mapper::toRecipeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getAllRecipes() {
        return recipeRepository.findAllWithDetails()
                .stream()
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
    public List<RecipeDTO> getRecipesByAuthorId(Long authorId) {
        return recipeRepository.findByAuthorId(authorId)
                .stream()
                .map(mapper::toRecipeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecipesByCategory(Long categoryId) {
        return recipeRepository.findByCategoryId(categoryId)
                .stream()
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
        recipe = recipeRepository.save(recipe);

        if (dto.getRecipeIngredients() != null && !dto.getRecipeIngredients().isEmpty()) {
            Set<RecipeIngredient> recipeIngredients = new HashSet<>();

            for (RecipeIngredientDTO riDto : dto.getRecipeIngredients()) {
                if (riDto.getIngredientName() == null || riDto.getIngredientName().isEmpty()) {
                    throw new IllegalArgumentException("Ingredient name is required");
                }

                UnitOfMeasure unit = (riDto.getUnitAbbreviation() == null || riDto.getUnitAbbreviation().isEmpty())
                        ? null
                        : unitRepository.findByAbbreviation(riDto.getUnitAbbreviation())
                        .orElseThrow(() -> new UnitNotFoundException("Unit not found: " + riDto.getUnitAbbreviation()));

                Ingredient ingredient = ingredientRepository.findByName(riDto.getIngredientName())
                        .orElseGet(() -> {
                            Ingredient newIng = new Ingredient();
                            newIng.setName(riDto.getIngredientName());
                            newIng.setUnit(unit);
                            return ingredientRepository.save(newIng);
                        });

                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.setRecipe(recipe);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setQuantity(riDto.getQuantity());
                recipeIngredient.setUnit(unit);

                recipeIngredients.add(recipeIngredient);
            }

            recipeIngredientRepository.saveAll(recipeIngredients);
            recipe.setRecipeIngredients(recipeIngredients);
        }

        return mapper.toRecipeDTO(recipe);
    }

    public RecipeDTO createRecipeWithoutTransaction(RecipeDTO dto) {
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
        recipe = recipeRepository.save(recipe);

        if (dto.getRecipeIngredients() != null && !dto.getRecipeIngredients().isEmpty()) {
            Set<RecipeIngredient> recipeIngredients = new HashSet<>();

            for (RecipeIngredientDTO riDto : dto.getRecipeIngredients()) {
                if (riDto.getIngredientName() == null || riDto.getIngredientName().isEmpty()) {
                    throw new IllegalArgumentException("Ingredient name is required");
                }
                UnitOfMeasure unit = (riDto.getUnitAbbreviation() == null || riDto.getUnitAbbreviation().isEmpty())
                        ? null
                        : unitRepository.findByAbbreviation(riDto.getUnitAbbreviation())
                        .orElseThrow(() -> new UnitNotFoundException("Unit not found: " + riDto.getUnitAbbreviation()));

                Ingredient ingredient = ingredientRepository.findByName(riDto.getIngredientName())
                        .orElseGet(() -> {
                            Ingredient newIng = new Ingredient();
                            newIng.setName(riDto.getIngredientName());
                            newIng.setUnit(unit);
                            return ingredientRepository.save(newIng);
                        });

                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.setRecipe(recipe);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setQuantity(riDto.getQuantity());
                recipeIngredient.setUnit(unit);

                recipeIngredients.add(recipeIngredient);
            }
            recipeIngredientRepository.saveAll(recipeIngredients);
        }

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

        if (dto.getRecipeIngredients() != null && !dto.getRecipeIngredients().isEmpty()) {
            recipeIngredientRepository.deleteAll(recipe.getRecipeIngredients());

            Set<RecipeIngredient> recipeIngredients = new HashSet<>();

            for (RecipeIngredientDTO riDto : dto.getRecipeIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(riDto.getIngredientId())
                        .orElseThrow(() -> new IngredientNotFoundException(
                                "Ingredient not found with id: " + riDto.getIngredientId()));

                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.setRecipe(recipe);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setQuantity(riDto.getQuantity());

                recipeIngredients.add(recipeIngredient);
            }

            recipeIngredientRepository.saveAll(recipeIngredients);
            recipe.setRecipeIngredients(recipeIngredients);
        }

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
}

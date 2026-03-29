package com.example.springrecipe.service;

import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.exceptions.CategoryNotFoundException;
import com.example.springrecipe.exceptions.RecipeNotFoundException;
import com.example.springrecipe.exceptions.UserNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Category;
import com.example.springrecipe.model.Recipe;
import com.example.springrecipe.model.User;
import com.example.springrecipe.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {
    @Mock
    RecipeRepository recipeRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    IngredientRepository ingredientRepository;
    @Mock
    UnitRepository unitRepository;
    @Mock
    RecipeIngredientRepository recipeIngredientRepository;
    @Mock
    RecipeMapper mapper;

    @InjectMocks
    RecipeService service;

    private RecipeDTO dto() {
        RecipeDTO dto = new RecipeDTO();
        dto.setName("Суп");
        dto.setAuthorId(1L);
        dto.setCategoryId(1L);
        dto.setCookingTime(30);
        return dto;
    }

    @Test
    void  getRecipeById_success() {
        Recipe recipe = new Recipe();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(mapper.toRecipeDTO(any())).thenReturn(dto());

        RecipeDTO result = service.getRecipeById(1L);

        assertEquals("Суп", result.getName());
    }

    @Test
    void getRecipeById_notFound() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> service.getRecipeById(1L));
    }

    @Test
    void createRecipe_success() {
        RecipeDTO dto = dto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(recipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toRecipeDTO(any())).thenReturn(dto);

        RecipeDTO result = service.createRecipe(dto);

        assertEquals("Суп", result.getName());
    }

    @Test
    void createRecipe_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.createRecipe(dto()));
    }

    @Test
    void createRecipe_categoryNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> service.createRecipe(dto()));
    }

    @Test
    void updateRecipe_success() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);

        RecipeDTO dto = dto();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(recipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toRecipeDTO(any())).thenReturn(dto());

        RecipeDTO result = service.updateRecipe(1L, dto());

        assertEquals("Суп", result.getName());
    }

    @Test
    void updateRecipe_notFound() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> service.updateRecipe(1L, dto()));
    }

    @Test
    void deleteRecipe_success() {
        when(recipeRepository.existsById(1L)).thenReturn(true);

        service.deleteRecipe(1L);

        verify(recipeRepository).deleteById(1L);
    }

    @Test
    void deleteRecipe_notFound() {
        when(recipeRepository.existsById(1L)).thenReturn(false);

        assertThrows(RecipeNotFoundException.class, () -> service.deleteRecipe(1L));
    }

    @Test
    void bulkCreateRecipesWithTransaction_success() {
        RecipeDTO dto = dto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(recipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toRecipeDTO(any())).thenReturn(dto);

        List<RecipeDTO> result = service.bulkCreateRecipesWithTransaction(List.of(dto));

        assertEquals(1, result.size());
    }

    @Test
    void bulkCreateRecipesWithTransaction_fail_all() {
        List<RecipeDTO> list = List.of(dto(), new RecipeDTO());

        assertThrows(Exception.class, () -> service.bulkCreateRecipesWithTransaction(list));
    }

    @Test
    void bulkCreateRecipesWithoutTransaction_partialSuccess() {
        RecipeDTO valid = dto();
        RecipeDTO invalid = new RecipeDTO();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(recipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toRecipeDTO(any())).thenReturn(valid);

        List<RecipeDTO> result = service.bulkCreateRecipesWithoutTransaction(List.of(valid, invalid));

        assertEquals(1, result.size());
    }
}

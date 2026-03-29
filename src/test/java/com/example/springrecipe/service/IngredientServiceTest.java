package com.example.springrecipe.service;

import com.example.springrecipe.dto.IngredientDTO;
import com.example.springrecipe.exceptions.IngredientInUseException;
import com.example.springrecipe.exceptions.IngredientNotFoundException;
import com.example.springrecipe.exceptions.UnitNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Ingredient;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.repository.IngredientRepository;
import com.example.springrecipe.repository.UnitRepository;
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
class IngredientServiceTest {
    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private RecipeMapper mapper;

    @InjectMocks
    private IngredientService ingredientService;

    private IngredientDTO dto(String name) {
        IngredientDTO dto = new IngredientDTO();
        dto.setName(name);
        return dto;
    }

    @Test
    void getAllIngredients_Success() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("Сахар");

        when(ingredientRepository.findAll()).thenReturn(List.of(ingredient));
        when(mapper.toIngredientDTO(any())).thenReturn(dto("Сахар"));

        List<IngredientDTO> result = ingredientService.getAllIngredients();

        assertEquals(1, result.size());
        verify(ingredientRepository).findAll();
    }

    @Test
    void getIngredientById_Success() {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName("Соль");

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient));
        when(mapper.toIngredientDTO(any())).thenReturn(dto("Соль"));

        IngredientDTO result = ingredientService.getIngredientById(1L);

        assertEquals("Соль", result.getName());
    }

    @Test
    void getIngredientById_NotFound() {
        when(ingredientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IngredientNotFoundException.class,
                () -> ingredientService.getIngredientById(1L));
    }

    @Test
    void createIngredient_Success() {
        IngredientDTO input = dto("Мука");

        when(ingredientRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toIngredientDTO(any())).thenReturn(input);

        IngredientDTO result = ingredientService.createIngredient(input);

        assertEquals("Мука", result.getName());
        verify(ingredientRepository).save(any());
    }

    @Test
    void createIngredient_WithUnit_Success() {
        IngredientDTO input = dto("Молоко");
        input.setUnitAbbreviation("мл");

        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setAbbreviation("мл");

        when(unitRepository.findByAbbreviation("мл")).thenReturn(Optional.of(unit));
        when(ingredientRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toIngredientDTO(any())).thenReturn(input);

        IngredientDTO result = ingredientService.createIngredient(input);

        assertEquals("Молоко", result.getName());
    }

    @Test
    void createIngredient_UnitNotFound() {
        IngredientDTO input = dto("Молоко");
        input.setUnitAbbreviation("мл");

        when(unitRepository.findByAbbreviation("мл")).thenReturn(Optional.empty());

        assertThrows(UnitNotFoundException.class, () -> ingredientService.createIngredient(input));
    }

    @Test
    void updateIngredient_Success() {
        Ingredient existing = new Ingredient();
        existing.setId(1L);
        existing.setName("Старое");

        IngredientDTO input = dto("Новое");

        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ingredientRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toIngredientDTO(any())).thenReturn(input);

        IngredientDTO result = ingredientService.updateIngredient(1L, input);

        assertEquals("Новое", result.getName());
    }

    @Test
    void updateIngredient_NotFound() {
        IngredientDTO dto = dto("test");
        when(ingredientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IngredientNotFoundException.class,
                () -> ingredientService.updateIngredient(1L, dto));
    }

    @Test
    void deleteIngredient_Success() {
        when(ingredientRepository.existsById(1L)).thenReturn(true);
        when(ingredientRepository.isIngredientUsedInAnyRecipe(1L)).thenReturn(false);

        ingredientService.deleteIngredient(1L);

        verify(ingredientRepository).deleteById(1L);
    }

    @Test
    void deleteIngredient_NotFound() {
        when(ingredientRepository.existsById(1L)).thenReturn(false);

        assertThrows(IngredientNotFoundException.class, () -> ingredientService.deleteIngredient(1L));
    }

    @Test
    void deleteIngredient_InUse() {
        when(ingredientRepository.existsById(1L)).thenReturn(true);
        when(ingredientRepository.isIngredientUsedInAnyRecipe(1L)).thenReturn(true);

        assertThrows(IngredientInUseException.class, () -> ingredientService.deleteIngredient(1L));
    }
}

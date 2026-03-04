package com.example.springrecipe.service;

import com.example.springrecipe.dto.IngredientDTO;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Ingredient;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.repository.IngredientRepository;
import com.example.springrecipe.repository.UnitRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final UnitRepository unitRepository;
    private final RecipeMapper mapper;

    @Transactional(readOnly = true)
    public List<IngredientDTO> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(mapper::toIngredientDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IngredientDTO getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        return mapper.toIngredientDTO(ingredient);
    }

    @Transactional
    public IngredientDTO createIngredient(IngredientDTO dto) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());

        if (dto.getUnitAbbreviation() != null) {
            UnitOfMeasure unit = unitRepository.findByAbbreviation(dto.getUnitAbbreviation())
                    .orElseThrow(() -> new RuntimeException("Unit not found"));
            ingredient.setUnit(unit);
        }

        ingredient = ingredientRepository.save(ingredient);
        return mapper.toIngredientDTO(ingredient);
    }

    @Transactional
    public IngredientDTO updateIngredient(Long id, IngredientDTO dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));

        ingredient.setName(dto.getName());

        if (dto.getUnitAbbreviation() != null) {
            UnitOfMeasure unit = unitRepository.findByAbbreviation(dto.getUnitAbbreviation())
                    .orElseThrow(() -> new RuntimeException(
                            "Unit not found with abbreviation: " + dto.getUnitAbbreviation()));
            ingredient.setUnit(unit);
        }
        ingredient = ingredientRepository.save(ingredient);
        return mapper.toIngredientDTO(ingredient);
    }

    @Transactional
    public void deleteIngredient(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingredient not found");
        }
        ingredientRepository.deleteById(id);
    }
}

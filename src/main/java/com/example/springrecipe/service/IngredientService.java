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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final UnitRepository unitRepository;
    private final RecipeMapper mapper;

    @Transactional(readOnly = true)
    public List<IngredientDTO> getAllIngredients() {
        log.debug("Запрос всех ингредиентов");
        List<IngredientDTO> result = ingredientRepository.findAll().stream()
                .map(mapper::toIngredientDTO)
                .toList();
        log.info("Найдено {} ингредиентов", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public IngredientDTO getIngredientById(Long id) {
        log.debug("Поиск ингредиента по ID: {}", id);
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ингредиент с ID {} не найден", id);
                    return new IngredientNotFoundException("Ingredient not found");
                });
        log.info("Найден ингредиент: {} (ID: {})", ingredient.getName(), ingredient.getId());
        return mapper.toIngredientDTO(ingredient);
    }

    @Transactional
    public IngredientDTO createIngredient(IngredientDTO dto) {
        log.info("Создание нового ингредиента: название='{}'", dto.getName());
        log.debug("Детали ингредиента: {}", dto);

        Ingredient ingredient = new Ingredient();
        ingredient.setName(dto.getName());

        if (dto.getUnitAbbreviation() != null) {
            log.debug("Поиск единицы измерения с аббревиатурой: {}", dto.getUnitAbbreviation());
            UnitOfMeasure unit = unitRepository.findByAbbreviation(dto.getUnitAbbreviation())
                    .orElseThrow(() -> {
                        log.error("Единица измерения '{}' не найдена", dto.getUnitAbbreviation());
                        return new UnitNotFoundException("Unit not found");
                    });
            ingredient.setUnit(unit);
            log.debug("Установлена единица измерения: {}", unit.getName());
        }

        ingredient = ingredientRepository.save(ingredient);
        log.info("Ингредиент успешно создан: ID={}, название='{}'", ingredient.getId(), ingredient.getName());
        return mapper.toIngredientDTO(ingredient);
    }

    @Transactional
    public IngredientDTO updateIngredient(Long id, IngredientDTO dto) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IngredientNotFoundException("Ingredient not found"));

        ingredient.setName(dto.getName());

        if (dto.getUnitAbbreviation() != null) {
            UnitOfMeasure unit = unitRepository.findByAbbreviation(dto.getUnitAbbreviation())
                    .orElseThrow(() -> new UnitNotFoundException(
                            "Unit not found with abbreviation: " + dto.getUnitAbbreviation()));
            ingredient.setUnit(unit);
        }
        ingredient = ingredientRepository.save(ingredient);
        return mapper.toIngredientDTO(ingredient);
    }

    @Transactional
    public void deleteIngredient(Long id) {
        log.warn("УДАЛЕНИЕ ИНГРЕДИЕНТА: ID={}", id);

        if (!ingredientRepository.existsById(id)) {
            log.warn("Ингредиент с ID {} не найден для удаления", id);
            throw new IngredientNotFoundException("Ingredient not found");
        }

        if (ingredientRepository.isIngredientUsedInAnyRecipe(id)) {
            log.warn("Ингредиент ID={} используется в рецептах, удаление запрещено", id);
            throw new IngredientInUseException("Ingredient can not be deleted because it is used in recipe");
        }

        ingredientRepository.deleteById(id);
        log.info("Ингредиент ID={} успешно удален", id);
    }
}

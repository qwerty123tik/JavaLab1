package com.example.springrecipe.service;

import com.example.springrecipe.dto.CategoryDTO;
import com.example.springrecipe.exceptions.CategoryAlreadyExists;
import com.example.springrecipe.exceptions.CategoryNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Category;
import com.example.springrecipe.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final RecipeMapper mapper;

    public List<CategoryDTO> getAllCategories() {
        log.debug("Запрос всех категорий");
        List<CategoryDTO> result = categoryRepository.findAll().stream()
                .map(mapper::toCategoryDTO)
                .toList();
        log.info("Найдено {} категорий", result.size());
        return result;
    }

    public CategoryDTO getCategoryById(Long id) {
        log.debug("Поиск категории по ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Категория с ID {} не найдена", id);
                    return new CategoryNotFoundException("Category not found");
                });
        log.info("Найдена категория: {} (ID: {})", category.getName(), category.getId());
        return mapper.toCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        log.info("Создание новой категории: название='{}'", dto.getName());

        if (categoryRepository.findByName(dto.getName()).isPresent()) {
            log.warn("Категория с названием '{}' уже существует", dto.getName());
            throw new CategoryAlreadyExists("Category already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        category = categoryRepository.save(category);
        log.info("Категория успешно создана: ID={}, название='{}'", category.getId(), category.getName());
        return mapper.toCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        log.info("Обновление категории: ID={}, название='{}'", id, dto.getName());

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Категория с ID {} не найдена для обновления", id);
                    return new CategoryNotFoundException("Category not found");
                });

        String oldName = category.getName();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        log.debug("Категория обновлена: '{}' -> '{}'", oldName, dto.getName());
        category = categoryRepository.save(category);
        log.info("Категория ID={} успешно обновлена", id);

        return mapper.toCategoryDTO(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.warn("УДАЛЕНИЕ КАТЕГОРИИ: ID={}", id);

        if (!categoryRepository.existsById(id)) {
            log.warn("Категория с ID {} не найдена для удаления", id);
            throw new CategoryNotFoundException("Category not found");
        }

        categoryRepository.deleteById(id);
        log.info("Категория ID={} успешно удалена", id);
    }
}

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
        List<CategoryDTO> result = categoryRepository.findAll().stream()
                .map(mapper::toCategoryDTO)
                .toList();
        log.info("Найдено {} категорий", result.size());
        return result;
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        log.info("Найдена категория: {} (ID: {})", category.getName(), category.getId());
        return mapper.toCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.findByName(dto.getName()).isPresent()) {
            throw new CategoryAlreadyExists("Category already exists");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        category = categoryRepository.save(category);
        return mapper.toCategoryDTO(category);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        category = categoryRepository.save(category);
        return mapper.toCategoryDTO(category);
    }

    @Transactional
    public void deleteCategory(Long id) {

        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}

package com.example.springrecipe.service;

import com.example.springrecipe.dto.CategoryDTO;
import com.example.springrecipe.exceptions.CategoryAlreadyExists;
import com.example.springrecipe.exceptions.CategoryNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Category;
import com.example.springrecipe.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private RecipeMapper mapper;
    @InjectMocks
    private CategoryService categoryService;

    private CategoryDTO dto(String name) {
        CategoryDTO dto = new CategoryDTO();
        dto.setName(name);
        return dto;
    }

    @Test
    @DisplayName("Успешное получение всех категорий")
    void getAllCategories_Success() {
        Category category = new Category();
        category.setName("Десерты");

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(mapper.toCategoryDTO(any())).thenReturn(dto("Десерты"));

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Успешное получение категории по ID")
    void getCategoryById_Success() {
        Long id = 1L;
        Category category = new Category();
        category.setId(id);
        category.setName("Завтраки");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(mapper.toCategoryDTO(any())).thenReturn(dto("Завтраки"));

        CategoryDTO result = categoryService.getCategoryById(id);

        assertEquals("Завтраки", result.getName());
    }

    @Test
    @DisplayName("Ошибка при поиске несуществующей категории")
    void getCategoryById_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(1L));
    }

    @Test
    @DisplayName("Успешное создание новой категории")
    void createCategory_Success() {
        CategoryDTO input = dto("Супы");

        when(categoryRepository.findByName("Супы")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toCategoryDTO(any())).thenReturn(input);

        CategoryDTO result = categoryService.createCategory(input);

        assertEquals("Супы", result.getName());
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("Ошибка создания: категория уже существует")
    void createCategory_AlreadyExists() {
        CategoryDTO input = dto("Салаты");

        when(categoryRepository.findByName("Салаты"))
                .thenReturn(Optional.of(new Category()));

        assertThrows(CategoryAlreadyExists.class,
                () -> categoryService.createCategory(input));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешное обновление категории")
    void updateCategory_Success() {
        Long id = 1L;

        Category existing = new Category();
        existing.setId(id);
        existing.setName("Старое");

        CategoryDTO input = dto("Новое");

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toCategoryDTO(any())).thenReturn(input);

        CategoryDTO result = categoryService.updateCategory(id, input);

        assertEquals("Новое", result.getName());
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("Ошибка обновления: категория не найдена")
    void updateCategory_NotFound() {
        CategoryDTO dto = dto("test");
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.updateCategory(1L, dto));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешное удаление категории")
    void deleteCategory_Success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Ошибка удаления: категория не существует")
    void deleteCategory_NotFound() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(1L));

        verify(categoryRepository, never()).deleteById(any());
    }
}

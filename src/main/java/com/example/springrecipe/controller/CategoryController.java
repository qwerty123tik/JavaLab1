package com.example.springrecipe.controller;

import com.example.springrecipe.dto.CategoryDTO;
import com.example.springrecipe.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
@Tag(name = "Категории", description = "Управление категориями рецептов (CRUD)")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "Получить все категории",
            description = "Возвращает список всех категорий"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Успешно получен список категорий"),
                           @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                                   content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(  @Parameter(description = "Название категории (фильтр)")
                                                                    @RequestParam(required = false) String name,
                                                                @Parameter(description = "Только категории с рецептами")
                                                                    @RequestParam(required = false)
                                                                Boolean withRecipes) {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(
            summary = "Получить категорию по ID",
            description = "Возвращает информацию о категории по её идентификатору"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Категория найдена"),
                           @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@Parameter(description = "ID категории",
            required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(
            summary = "Создать новую категорию",
            description = "Создает новую категорию. Название должно быть уникальным."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Категория успешно создана"),
                           @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content),
                           @ApiResponse(responseCode = "409", description = "Категория с таким названием уже сущ",
                                   content = @Content)
    })
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        return new ResponseEntity<>(categoryService.createCategory(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Обновить категорию",
            description = "Обновляет существующую категорию по её ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Категория успешно обновлена"),
                           @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content),
                           @ApiResponse(responseCode = "409", description = "Название категории уже используется",
                                   content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory( @Parameter(description = "ID категории",
            required = true, example = "1") @PathVariable Long id, @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @Operation(
            summary = "Удалить категорию",
            description = "Удаляет категорию по её ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Категория успешно удалена"),
                           @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@Parameter(description = "ID категории",
            required = true, example = "1") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

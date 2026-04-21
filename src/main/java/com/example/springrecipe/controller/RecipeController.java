package com.example.springrecipe.controller;

import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recipes")
@AllArgsConstructor
@Tag(name = "Рецепты", description = "Управление рецептами (CRUD, поиск, пагинация, кэширование)")
public class RecipeController {
    private final RecipeService recipeService;

    @Operation(
            summary = "Получить все рецепты",
            description = "Возвращает список всех рецептов с пагинацией и сортировкой"
    )
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Успешно получен список рецептов"),
                            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes(
            @Parameter(description = "Максимальное время приготовления в минутах")
                                                          @RequestParam(required = false) Integer maxTime,
                                                          @Parameter(description = "ID категории")
                                                          @RequestParam(required = false) Long categoryId,
                                                          @Parameter(description = "Название рецепта")
                                                          @RequestParam(required = false) String title) {

        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @Operation(
            summary = "Демонстрация проблемы N+1",
            description = "Возвращает список рецептов с проблемой N+1 (много запросов к БД)"
    )
    @GetMapping("/demo/NPlusOne")
    public ResponseEntity<List<RecipeDTO>> getAllRecipesWithNPlusOneProblem() {
        return ResponseEntity.ok(recipeService.getAllRecipesWithNPlusOneProblem());
    }

    @Operation(
            summary = "Решение проблемы N+1 через EntityGraph",
            description = "Возвращает список рецептов с оптимизированным запросом (один запрос к БД)"
    )
    @GetMapping("/solutionProblem")
    public ResponseEntity<List<RecipeDTO>> getAllRecipesWithEntityGraph() {
        return ResponseEntity.ok(recipeService.getAllRecipesWithEntityGraph());
    }

    @Operation(
            summary = "Поиск рецептов (JPQL)",
            description = "Поиск рецептов по ингредиенту и/или категории с пагинацией"
    )
    @GetMapping("/search/jpql")
    public ResponseEntity<Page<RecipeDTO>> searchRecipesJPQL(
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        Page<RecipeDTO> result = recipeService.searchRecipesJPQL(ingredient, category, title, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Поиск рецептов (Native SQL)",
            description = "Поиск рецептов с использованием нативного SQL запроса"
    )
    @GetMapping("/search/native")
    public ResponseEntity<Page<RecipeDTO>> searchRecipesNative(
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<RecipeDTO> result = recipeService.searchRecipesNative(ingredient, category, title, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Очистить кэш",
            description = "Принудительная инвалидация in-memory кэша"
    )
    @PostMapping("/cache/invalidate")
    public ResponseEntity<String> invalidateCache() {
        return ResponseEntity.ok("Кэш успешно инвалидирован. При следующем поиске данные будут загружены из БД.");
    }

    @Operation(
            summary = "Статистика кэша",
            description = "Возвращает статистику in-memory кэша: размер, ключи, количество хитов"
    )
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        return ResponseEntity.ok(recipeService.getCacheStatistics());
    }

    @Operation(
            summary = "Получить рецепт по ID",
            description = "Возвращает полную информацию о рецепте по его идентификатору"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Рецепт найден"),
                           @ApiResponse(responseCode = "404", description = "Рецепт не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById( @Parameter(description = "ID рецепта",
            required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @Operation(
            summary = "Получить рецепты автора",
            description = "Возвращает список рецептов по ID автора"
    )
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByAuthorId( @Parameter(description = "ID автора",
            required = true, example = "1") @PathVariable long authorId) {
        return ResponseEntity.ok(recipeService.getRecipesByAuthorId(authorId));
    }

    @Operation(
            summary = "Получить рецепты категории",
            description = "Возвращает список рецептов по ID категории"
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByCategory( @Parameter(description = "ID категории",
            required = true, example = "1") @PathVariable Long categoryId) {
        return ResponseEntity.ok(recipeService.getRecipesByCategory(categoryId));
    }

    @Operation(
            summary = "Создать новый рецепт",
            description = "Создает новый рецепт с ингредиентами. Ингредиенты создаются автоматически, если не сущ"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Рецепт успешно создан"),
                           @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content),
                           @ApiResponse(responseCode = "404", description = "Автор или категория не найдены",
                                   content = @Content)
    })
    @PostMapping
    public ResponseEntity<RecipeDTO> createRecipe(@Valid @RequestBody RecipeDTO dto) {
        return new ResponseEntity<>(recipeService.createRecipe(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Создать рецепт без транзакции (демонстрация)",
            description = "Создает рецепт без @Transactional. При ошибке данные могут сохраниться частично"
    )
    @PostMapping("/demo/withoutTransaction")
    public ResponseEntity<Object> createRecipeWithoutTransaction(@Valid @RequestBody RecipeDTO dto) {
        try {
            recipeService.createRecipeWithoutTransaction(dto);
            return ResponseEntity.ok("Рецепт создан");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage() +
                            "\nно! Рецепт мог сохраниться в БД. Проверьте таблицу recipes");
        }
    }

    @Operation(
            summary = "Создать рецепт с транзакцией (демонстрация)",
            description = "Создает рецепт с @Transactional. При ошибке происходит полный откат изменений"
    )
    @PostMapping("/demo/withTransaction")
    public ResponseEntity<Object> createRecipeWithTransaction(@Valid @RequestBody RecipeDTO dto) {
        try {
            recipeService.createRecipe(dto);
            return ResponseEntity.ok("Рецепт создан");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage() +
                            "\nблагодаря @Transactional ничего не сохранилось!");
        }
    }

    @Operation(
            summary = "Обновить рецепт",
            description = "Обновляет существующий рецепт по его ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Рецепт успешно обновлен"),
                           @ApiResponse(responseCode = "404", description = "Рецепт не найден", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> updateRecipe(@Parameter(description = "ID рецепта", required = true)
                                                      @PathVariable Long id,
                                                  @Valid @RequestBody RecipeDTO dto) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, dto));
    }

    @Operation(
            summary = "Удалить рецепт",
            description = "Удаляет рецепт по его ID. Кэш автоматически инвалидируется"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Рецепт успешно удален"),
                           @ApiResponse(responseCode = "404", description = "Рецепт не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe( @Parameter(description = "ID рецепта",
            required = true) @PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Массовое создание рецептов (Транзакционно)",
            description = "Создает несколько рецептов. Если хотя бы один рецепт невалиден или " +
                    "произойдет ошибка — не сохранится НИЧЕГО (откат транзакции)"
    )
    @PostMapping("/bulk")
    public ResponseEntity<List<RecipeDTO>> bulkCreateWithTransaction(
            @RequestBody List<RecipeDTO> recipes) {
        List<RecipeDTO> result = recipeService.bulkCreateRecipesWithTransaction(recipes);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Массовое создание без транзакции (Частичное)",
            description = "Пытается создать каждый рецепт по отдельности. " +
                    "Ошибки в одном рецепте не мешают сохранению других"
    )
    @PostMapping("/bulk/without-transaction")
    public ResponseEntity<List<RecipeDTO>> bulkCreateWithoutTransaction(
            @RequestBody List<RecipeDTO> recipes) {
        List<RecipeDTO> result = recipeService.bulkCreateRecipesWithoutTransaction(recipes);
        return ResponseEntity.ok(result);
    }
}

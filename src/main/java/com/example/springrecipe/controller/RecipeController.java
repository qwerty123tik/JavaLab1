package com.example.springrecipe.controller;

import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.service.RecipeService;
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
public class RecipeController {
    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes( @RequestParam(required = false) Integer maxTime,
                                                          @RequestParam(required = false) Long categoryId,
                                                          @RequestParam(required = false) String title) {

        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/demo/NPlusOne")
    public ResponseEntity<List<RecipeDTO>> getAllRecipesWithNPlusOneProblem() {
        return ResponseEntity.ok(recipeService.getAllRecipesWithNPlusOneProblem());
    }

    @GetMapping("/solutionProblem")
    public ResponseEntity<List<RecipeDTO>> getAllRecipesWithEntityGraph() {
        return ResponseEntity.ok(recipeService.getAllRecipesWithEntityGraph());
    }

    @GetMapping("/search/jpql")
    public ResponseEntity<Page<RecipeDTO>> searchRecipesJPQL(
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        String ingredientParam = (ingredient == null || ingredient.isEmpty()) ? null : ingredient;
        String categoryParam = (category == null || category.isEmpty()) ? null : category;

        Page<RecipeDTO> result = recipeService.searchRecipesJPQL(ingredientParam, categoryParam, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/native")
    public ResponseEntity<Page<RecipeDTO>> searchRecipesNative(
            @RequestParam(required = false) String ingredient,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {

        Page<RecipeDTO> result = recipeService.searchRecipesNative(ingredient, category, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cache/invalidate")
    public ResponseEntity<String> invalidateCache() {
        return ResponseEntity.ok("Кэш успешно инвалидирован. При следующем поиске данные будут загружены из БД.");
    }

    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        return ResponseEntity.ok(recipeService.getCacheStatistics());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByAuthorId(@PathVariable long authorId) {
        return ResponseEntity.ok(recipeService.getRecipesByAuthorId(authorId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(recipeService.getRecipesByCategory(categoryId));
    }

    @PostMapping
    public ResponseEntity<RecipeDTO> createRecipe(@RequestBody RecipeDTO dto) {
        return new ResponseEntity<>(recipeService.createRecipe(dto), HttpStatus.CREATED);
    }

    @PostMapping("/demo/withoutTransaction")
    public ResponseEntity<Object> createRecipeWithoutTransaction(@RequestBody RecipeDTO dto) {
        try {
            recipeService.createRecipeWithoutTransaction(dto);
            return ResponseEntity.ok("Рецепт создан");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage() +
                            "\nно! Рецепт мог сохраниться в БД. Проверьте таблицу recipes.");
        }
    }

    @PostMapping("/demo/withTransaction")
    public ResponseEntity<Object> createRecipeWithTransaction(@RequestBody RecipeDTO dto) {
        try {
            recipeService.createRecipe(dto);
            return ResponseEntity.ok("Рецепт создан");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage() +
                            "\nблагодаря @Transactional нисего не сохранилось!");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> updateRecipe(@PathVariable Long id, @RequestBody RecipeDTO dto) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}

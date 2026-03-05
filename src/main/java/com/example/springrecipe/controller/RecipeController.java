package com.example.springrecipe.controller;

import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.service.RecipeService;
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

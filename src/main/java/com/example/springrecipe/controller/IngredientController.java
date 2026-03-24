package com.example.springrecipe.controller;

import com.example.springrecipe.dto.IngredientDTO;
import com.example.springrecipe.service.IngredientService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@AllArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;

    @GetMapping
    public ResponseEntity<List<IngredientDTO>> getAllIngredients() {
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientDTO> getIngredientById(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @PostMapping
    public ResponseEntity<IngredientDTO> createIngredient(@Valid @RequestBody IngredientDTO dto) {
        return new ResponseEntity<>(ingredientService.createIngredient(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientDTO> updateIngredient(@PathVariable Long id,
                                                          @Valid @RequestBody IngredientDTO dto) {
        return ResponseEntity.ok(ingredientService.updateIngredient(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}

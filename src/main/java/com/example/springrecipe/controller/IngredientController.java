package com.example.springrecipe.controller;

import com.example.springrecipe.dto.IngredientDTO;
import com.example.springrecipe.service.IngredientService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@AllArgsConstructor
@Tag(name = "Ингредиенты", description = "Управление ингредиентами (CRUD)")
public class IngredientController {
    private final IngredientService ingredientService;

    @Operation(
            summary = "Получить все ингредиенты",
            description = "Возвращает список всех ингредиентов"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Успешно получен список ингредиентов"),
                           @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                                   content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<IngredientDTO>> getAllIngredients() {
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }

    @Operation(
            summary = "Получить ингредиент по ID",
            description = "Возвращает информацию об ингредиенте по его идентификатору"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ингредиент найден"),
                           @ApiResponse(responseCode = "404", description = "Ингредиент не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<IngredientDTO> getIngredientById( @Parameter(description = "ID ингредиента",
            required = true, example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @Operation(
            summary = "Создать новый ингредиент",
            description = "Создает новый ингредиент с указанной единицей измерения"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Ингредиент успешно создан"),
                           @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content),
                           @ApiResponse(responseCode = "404", description = "Единица измерения не найдена",
                                   content = @Content)
    })
    @PostMapping
    public ResponseEntity<IngredientDTO> createIngredient(@Valid @RequestBody IngredientDTO dto) {
        return new ResponseEntity<>(ingredientService.createIngredient(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Обновить ингредиент",
            description = "Обновляет существующий ингредиент по его ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Ингредиент успешно обновлен"),
                           @ApiResponse(responseCode = "404", description = "Ингредиент не найден", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<IngredientDTO> updateIngredient(@Parameter(description = "ID ингредиента", required = true,
                                                                      example = "1") @PathVariable Long id,
                                                          @Valid @RequestBody IngredientDTO dto) {
        return ResponseEntity.ok(ingredientService.updateIngredient(id, dto));
    }

    @Operation(
            summary = "Удалить ингредиент",
            description = "Удаляет ингредиент по его ID. Если ингредиент используется в рецептах, удаление запрещено."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Ингредиент успешно удален"),
                           @ApiResponse(responseCode = "404", description = "Ингредиент не найден", content = @Content),
                           @ApiResponse(responseCode = "409", description = "Ингредиент используется в рецептах",
                                   content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient( @Parameter(description = "ID ингредиента",
            required = true, example = "1") @PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}

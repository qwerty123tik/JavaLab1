package com.example.springrecipe.controller;

import com.example.springrecipe.dto.UnitDTO;
import com.example.springrecipe.service.UnitService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/units")
@AllArgsConstructor
@Tag(name = "Единицы измерения", description = "Управление единицами измерения ингредиентов (CRUD)")
public class UnitController {
    private final UnitService unitService;

    @Operation(
            summary = "Получить все единицы измерения",
            description = "Возвращает список всех единиц измерения"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Успешно получен список единиц измерения"),
                           @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                                   content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UnitDTO>> getAllUnits() {
        return ResponseEntity.ok(unitService.getAllUnits());
    }

    @Operation(
            summary = "Создать новую единицу измерения",
            description = "Создает новую единицу измерения. Аббревиатура должна быть уникальной."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Единица измерения успешно создана"),
                           @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content),
                           @ApiResponse(responseCode = "409", description = "Аббревиатура уже существует",
                                   content = @Content)
    })
    @PostMapping
    public ResponseEntity<UnitDTO> createUnit(@Valid @RequestBody UnitDTO dto) {
        return new ResponseEntity<>(unitService.createUnit(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Удалить единицу измерения",
            description = "Удаляет единицу измерения по её ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Единица измерения успешно удалена"),
                           @ApiResponse(responseCode = "404", description = "Единица измерения не найдена",
                                   content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUnit(@Parameter(description = "ID единицы измерения",
            required = true, example = "1") @PathVariable Long id) {
        unitService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.springrecipe.controller;

import com.example.springrecipe.dto.ReviewDTO;
import com.example.springrecipe.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
@AllArgsConstructor
@Tag(name = "Отзывы", description = "Управление отзывами на рецепты (CRUD)")
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(
            summary = "Получить все отзывы",
            description = "Возвращает список всех отзывов"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Успешно получен список отзывов"),
                           @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                                   content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @Operation(
            summary = "Получить отзыв по ID",
            description = "Возвращает информацию об отзыве по его идентификатору"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Отзыв найден"),
                           @ApiResponse(responseCode = "404", description = "Отзыв не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@Parameter(description = "ID отзыва", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @Operation(
            summary = "Получить отзывы по рецепту",
            description = "Возвращает все отзывы для указанного рецепта"
    )
    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByRecipe(@Parameter(description = "ID рецепта",
            required = true, example = "1") @PathVariable Long recipeId) {
        return ResponseEntity.ok(reviewService.getReviewsByRecipe(recipeId));
    }

    @Operation(
            summary = "Получить отзывы пользователя",
            description = "Возвращает все отзывы, оставленные указанным пользователем"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUser( @Parameter(description = "ID пользователя",
            required = true, example = "1") @PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @Operation(
            summary = "Создать новый отзыв",
            description = "Создает новый отзыв на рецепт. Оценка от 1 до 5."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Отзыв успешно создан"),
                           @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content),
                           @ApiResponse(responseCode = "404", description = "Пользователь или рецепт не найдены",
                                   content = @Content)
    })
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody ReviewDTO dto) {
        return new ResponseEntity<>(reviewService.createReview(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Обновить отзыв",
            description = "Обновляет существующий отзыв по его ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Отзыв успешно обновлен"),
                           @ApiResponse(responseCode = "404", description = "Отзыв не найден", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(  @Parameter(description = "ID отзыва",
            required = true, example = "1") @PathVariable Long id, @Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.ok(reviewService.updateReview(id, dto));
    }

    @Operation(
            summary = "Удалить отзыв",
            description = "Удаляет отзыв по его ID"
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Отзыв успешно удален"),
                           @ApiResponse(responseCode = "404", description = "Отзыв не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview( @Parameter(description = "ID отзыва",
            required = true, example = "1") @PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}

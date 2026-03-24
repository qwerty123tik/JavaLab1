package com.example.springrecipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Формат ответа при ошибке")
public class ErrorResponse {
    @Schema(description = "HTTP статус код", example = "400")
    private int status;

    @Schema(description = "Название ошибки", example = "Ошибка валидации")
    private String error;

    @Schema(description = "Подробное сообщение об ошибке", example = "Проверьте правильность заполнения полей")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/v1/recipes")
    private String path;

    @Schema(description = "Детали ошибок валидации по полям")
    private Map<String, String> validationErrors;
}

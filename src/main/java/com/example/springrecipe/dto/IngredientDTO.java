package com.example.springrecipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для ингредиентов")
public class IngredientDTO {
    @Schema(description = "ID ингредиента", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Название ингредиента обязательно")
    @Schema(description = "Название ингредиента", example = "Мука", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Название единицы измерения", example = "грамм", accessMode = Schema.AccessMode.READ_ONLY)
    private String unitName;

    @Schema(description = "Аббревиация единицы измерения", example = "г", accessMode = Schema.AccessMode.READ_ONLY)
    private String unitAbbreviation;
}

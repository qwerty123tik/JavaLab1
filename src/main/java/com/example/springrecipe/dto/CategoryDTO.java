package com.example.springrecipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для категорий")
public class CategoryDTO {
    @Schema(description = "ID категории", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Название категории обязательно")
    @Schema(description = "Название категории", example = "Десерты", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    @Schema(description = "Описание категории", example = "Сладкие блюда")
    private String description;

    @Schema(description = "Количество рецептов в категории", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Long recipeCount;
}

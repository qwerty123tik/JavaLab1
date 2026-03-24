package com.example.springrecipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для меры измерения")
public class UnitDTO {
    @Schema(description = "ID меры измерения", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Название единицы измерения", example = "грамм", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Аббревиация единицы измерения", example = "г", requiredMode = Schema.RequiredMode.REQUIRED)
    private String abbreviation;
}

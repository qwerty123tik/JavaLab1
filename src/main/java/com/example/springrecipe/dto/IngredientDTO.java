package com.example.springrecipe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientDTO {
    private Long id;
    @NotBlank(message = "Название ингредиента обязательно")
    private String name;
    private String unitName;
    private String unitAbbreviation;
}

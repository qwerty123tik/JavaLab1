package com.example.springrecipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    private Long id;

    @NotBlank(message = "Название категории обязательно")
    private String name;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;
    private Long recipeCount;
}

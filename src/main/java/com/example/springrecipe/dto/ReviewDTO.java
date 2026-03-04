package com.example.springrecipe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long id;
    private Integer rating;
    private String comment;
    //private LocalDateTime creationDate;

    private Long userId;
    private String userName;

    private Long recipeId;
    private String recipeName;
}

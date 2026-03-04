package com.example.springrecipe.exceptions;

public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException (String massage) {
        super(massage);
    }
}

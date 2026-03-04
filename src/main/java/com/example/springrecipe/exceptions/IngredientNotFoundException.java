package com.example.springrecipe.exceptions;

public class IngredientNotFoundException extends RuntimeException {
    public IngredientNotFoundException (String massage) {
        super(massage);
    }
}

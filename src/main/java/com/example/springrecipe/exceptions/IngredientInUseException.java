package com.example.springrecipe.exceptions;

public class IngredientInUseException extends RuntimeException {
    public IngredientInUseException (String massage) {
        super(massage);
    }
}

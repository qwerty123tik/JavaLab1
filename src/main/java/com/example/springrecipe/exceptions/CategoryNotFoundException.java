package com.example.springrecipe.exceptions;

public class CategoryNotFoundException extends RuntimeException {
    public static final String DEFAULT_MESSAGE = "Category not found";

    public CategoryNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public CategoryNotFoundException(String massage) {
        super(massage);
    }
}

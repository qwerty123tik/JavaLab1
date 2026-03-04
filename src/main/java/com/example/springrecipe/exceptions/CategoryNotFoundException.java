package com.example.springrecipe.exceptions;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String massage) {
        super(massage);
    }
}

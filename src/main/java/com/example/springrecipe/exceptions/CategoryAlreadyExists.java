package com.example.springrecipe.exceptions;

public class CategoryAlreadyExists extends RuntimeException {
    public CategoryAlreadyExists (String massage) {
        super(massage);
    }
}

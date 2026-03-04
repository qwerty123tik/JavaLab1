package com.example.springrecipe.exceptions;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException (String massage) {
        super(massage);
    }
}

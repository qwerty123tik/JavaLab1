package com.example.springrecipe.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException (String massage) {
        super(massage);
    }
}

package com.example.springrecipe.exceptions;

public class EmailAlreadyExists extends RuntimeException {
    public EmailAlreadyExists (String massage) {
        super(massage);
    }
}

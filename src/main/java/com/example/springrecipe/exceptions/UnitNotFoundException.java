package com.example.springrecipe.exceptions;

public class UnitNotFoundException extends RuntimeException {
    public UnitNotFoundException (String massage) {
        super(massage);
    }
}

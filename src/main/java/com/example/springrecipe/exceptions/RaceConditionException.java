package com.example.springrecipe.exceptions;

public class RaceConditionException extends RuntimeException {
    public RaceConditionException(String message) {
        super(message);
    }

    public RaceConditionException(String message, Throwable cause) {
        super(message, cause);
    }
}

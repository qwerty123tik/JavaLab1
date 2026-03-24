package com.example.springrecipe.exceptions;

import com.example.springrecipe.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации",
                "Проверьте правильность заполнения полей",
                request,
                errors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка. Пожалуйста, попробуйте позже.",
                request,
                null
        );
    }

    @ExceptionHandler({RecipeNotFoundException.class, UserNotFoundException.class,
                       UnitNotFoundException.class, ReviewNotFoundException.class,
                       IngredientNotFoundException.class, CategoryNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(RuntimeException ex, WebRequest request) {
        return  buildResponse(
                HttpStatus.NOT_FOUND,
                "Ресурс не найден",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler({CategoryAlreadyExists.class, EmailAlreadyExists.class, IngredientInUseException.class})
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(RuntimeException ex, WebRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Конфликт данных",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Некорректный запрос",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String message = String.format("Параметр '%s' должен быть типа %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "число");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Некорректный тип параметра",
                message,
                request,
                null
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Метод не поддерживается",
                ex.getMessage(),
                request,
                null
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse (HttpStatus status, String error,
                                                         String message, WebRequest request,
                                                         Map<String, String> validationErrors) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getDescription(false).replace("URI = ", ""))
                .validationErrors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}

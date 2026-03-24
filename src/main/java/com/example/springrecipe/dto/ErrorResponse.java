package com.example.springrecipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}

package com.example.payflow.shared;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {

    private final T data;
    private final String message;
    private final int statusCode;

    private ApiResponse(T data, String message, HttpStatus status) {
        this.data = data;
        this.message = message;
        this.statusCode = status.value();
    }

    public static <T> ApiResponse<T> success(T data, String message, HttpStatus status) {
        return new ApiResponse<>(data, message, status);
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        return new ApiResponse<>(null, message, status);
    }
}

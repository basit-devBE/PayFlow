package com.example.payflow.shared;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException ex) {
        return buildError(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        return buildError(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        return buildError("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> buildError(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, status));
    }
}

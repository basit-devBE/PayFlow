package com.example.payflow.shared;

public abstract class NotFoundException extends RuntimeException {
    protected NotFoundException(String message) {
        super(message);
    }
}

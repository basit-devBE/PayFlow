package com.example.payflow.merchant;

public class MerchantAlreadyExistsException extends RuntimeException {
    public MerchantAlreadyExistsException(String email) {
        super("Merchant already registered with email: " + email);
    }
}

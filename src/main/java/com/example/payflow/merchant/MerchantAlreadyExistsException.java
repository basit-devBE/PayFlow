package com.example.payflow.merchant;

import com.example.payflow.shared.DomainException;

public class MerchantAlreadyExistsException extends DomainException {
    public MerchantAlreadyExistsException(String email) {
        super("Merchant already registered with email: " + email);
    }
}

package com.example.payflow.payments;

import com.example.payflow.shared.NotFoundException;

import java.util.UUID;

public class PaymentNotFoundException extends NotFoundException {
    public PaymentNotFoundException(UUID id) {
        super("Payment not found with id: " + id);
    }
}

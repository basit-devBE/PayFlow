package com.example.payflow.fraud;

import com.example.payflow.shared.NotFoundException;

import java.util.UUID;

public class FraudAssessmentNotFoundException extends NotFoundException {

    public FraudAssessmentNotFoundException(UUID transactionId) {
        super("Fraud assessment not found for transaction: " + transactionId);
    }
}

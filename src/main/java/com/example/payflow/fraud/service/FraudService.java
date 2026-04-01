package com.example.payflow.fraud.service;

import com.example.payflow.fraud.FraudAssessmentNotFoundException;
import com.example.payflow.fraud.api.response.FraudAssessmentResponse;
import com.example.payflow.fraud.infra.FraudAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FraudService {

    private final FraudAssessmentRepository fraudAssessmentRepository;

    public FraudAssessmentResponse findByTransactionId(UUID merchantId, UUID transactionId) {
        return fraudAssessmentRepository.findByTransactionIdAndMerchantId(transactionId, merchantId)
                .map(assessment -> new FraudAssessmentResponse(
                        assessment.getId(),
                        assessment.getTransactionId(),
                        assessment.getScore(),
                        assessment.getDecision().name(),
                        assessment.getAssessedAt()
                ))
                .orElseThrow(() -> new FraudAssessmentNotFoundException(transactionId));
    }
}

package com.example.payflow.fraud.infra;

import com.example.payflow.fraud.domain.FraudAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FraudAssessmentRepository extends JpaRepository<FraudAssessment, UUID> {
    Optional<FraudAssessment> findByTransactionId(UUID transactionId);
    Optional<FraudAssessment> findByTransactionIdAndMerchantId(UUID transactionId, UUID merchantId);
}

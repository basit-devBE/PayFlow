package com.example.payflow.fraud.infra;

import com.example.payflow.fraud.domain.FraudAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FraudAssessmentRepository extends JpaRepository<FraudAssessment, UUID> {
}

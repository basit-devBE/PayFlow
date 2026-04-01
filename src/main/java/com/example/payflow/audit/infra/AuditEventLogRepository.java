package com.example.payflow.audit.infra;

import com.example.payflow.audit.domain.AuditEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventLogRepository extends JpaRepository<AuditEventLog, UUID> {

    List<AuditEventLog> findByCorrelationIdOrderByOccurredAtAsc(UUID correlationId);
    List<AuditEventLog> findByMerchantIdOrderByOccurredAtDesc(UUID merchantId);
    List<AuditEventLog> findByMerchantIdAndPaymentIdOrderByOccurredAtAsc(UUID merchantId, UUID paymentId);
}

package com.payment.payment.service;

import com.payment.payment.domain.AuditLog;
import com.payment.payment.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentAuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String resourceType, String resourceId, UUID accountId, String details) {
        try {
            auditLogRepository.save(AuditLog.builder()
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .accountId(accountId)
                .details(details)
                .build());
        } catch (Exception e) {
            // do not fail the main flow
        }
    }
}

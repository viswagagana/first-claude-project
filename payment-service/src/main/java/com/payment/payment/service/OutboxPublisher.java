package com.payment.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.payment.domain.OutboxEvent;
import com.payment.payment.domain.Payment;
import com.payment.payment.domain.Refund;
import com.payment.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_PAYMENT = "payment.events";
    private static final String TOPIC_REFUND = "refund.events";

    @Transactional(rollbackFor = Exception.class)
    public void publishPaymentCaptured(Payment payment) {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of(
            "type", "PaymentCaptured",
            "eventId", eventId,
            "timestamp", Instant.now().toString(),
            "paymentId", payment.getId().toString(),
            "accountId", payment.getAccountId().toString(),
            "currency", payment.getCurrency(),
            "amount", payment.getAmount().toString(),
            "gateway", payment.getGateway(),
            "gatewayCaptureId", payment.getGatewayCaptureId() != null ? payment.getGatewayCaptureId() : ""
        );
        OutboxEvent event = OutboxEvent.builder()
            .aggregateType("Payment")
            .aggregateId(payment.getId().toString())
            .eventType("PaymentCaptured")
            .payload(writeJson(payload))
            .build();
        outboxEventRepository.save(event);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishPaymentFailed(Payment payment, String reason) {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of(
            "type", "PaymentFailed",
            "eventId", eventId,
            "timestamp", Instant.now().toString(),
            "paymentId", payment.getId().toString(),
            "accountId", payment.getAccountId().toString(),
            "currency", payment.getCurrency(),
            "amount", payment.getAmount().toString(),
            "reason", reason != null ? reason : "unknown",
            "gateway", payment.getGateway()
        );
        OutboxEvent event = OutboxEvent.builder()
            .aggregateType("Payment")
            .aggregateId(payment.getId().toString())
            .eventType("PaymentFailed")
            .payload(writeJson(payload))
            .build();
        outboxEventRepository.save(event);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishRefundCompleted(Refund refund, Payment payment) {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of(
            "type", "RefundCompleted",
            "eventId", eventId,
            "timestamp", Instant.now().toString(),
            "refundId", refund.getId().toString(),
            "paymentId", payment.getId().toString(),
            "accountId", payment.getAccountId().toString(),
            "currency", refund.getCurrency(),
            "amount", payment.getAmount().toString(),
            "refundAmount", refund.getAmount().toString(),
            "gateway", payment.getGateway(),
            "gatewayRefundId", refund.getGatewayRefundId() != null ? refund.getGatewayRefundId() : ""
        );
        OutboxEvent event = OutboxEvent.builder()
            .aggregateType("Refund")
            .aggregateId(refund.getId().toString())
            .eventType("RefundCompleted")
            .payload(writeJson(payload))
            .build();
        outboxEventRepository.save(event);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishRefundFailed(Refund refund, Payment payment, String reason) {
        String eventId = UUID.randomUUID().toString();
        Map<String, Object> payload = Map.of(
            "type", "RefundFailed",
            "eventId", eventId,
            "timestamp", Instant.now().toString(),
            "refundId", refund.getId().toString(),
            "paymentId", payment.getId().toString(),
            "accountId", payment.getAccountId().toString(),
            "reason", reason != null ? reason : "unknown",
            "gateway", payment.getGateway()
        );
        OutboxEvent event = OutboxEvent.builder()
            .aggregateType("Refund")
            .aggregateId(refund.getId().toString())
            .eventType("RefundFailed")
            .payload(writeJson(payload))
            .build();
        outboxEventRepository.save(event);
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

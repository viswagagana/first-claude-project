package com.payment.payment.service;

import com.payment.payment.domain.Payment;
import com.payment.payment.domain.Refund;
import com.payment.payment.gateway.GatewayRequest;
import com.payment.payment.gateway.GatewayResponse;
import com.payment.payment.gateway.PayPalGateway;
import com.payment.payment.gateway.StripeGateway;
import com.payment.payment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOrchestrationService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final StripeGateway stripeGateway;
    private final PayPalGateway payPalGateway;
    private final OutboxPublisher outboxPublisher;
    private final PaymentAuditService auditService;

    @Transactional
    public Payment createPayment(UUID accountId, UUID paymentMethodId, String gateway, String paymentMethodGatewayId,
                                 BigDecimal amount, String currency, String idempotencyKey, String description) {
        if (idempotencyKey != null) {
            var existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        GatewayRequest req = GatewayRequest.builder()
            .accountId(accountId)
            .paymentMethodId(paymentMethodGatewayId)
            .amount(amount)
            .currency(currency)
            .idempotencyKey(idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())
            .description(description)
            .build();
        GatewayResponse res = "stripe".equalsIgnoreCase(gateway)
            ? stripeGateway.authorizeAndCapture(req)
            : payPalGateway.authorizeAndCapture(req);
        Payment payment = Payment.builder()
            .accountId(accountId)
            .idempotencyKey(idempotencyKey)
            .gateway(gateway)
            .gatewayPaymentId(res.getGatewayPaymentId())
            .gatewayCaptureId(res.getGatewayCaptureId())
            .paymentMethodId(paymentMethodId)
            .amount(amount)
            .currency(currency)
            .status(res.isSuccess() ? "CAPTURED" : "FAILED")
            .description(description)
            .build();
        payment = paymentRepository.save(payment);
        auditService.log("PAYMENT_CREATE", "payment", payment.getId().toString(), accountId, res.isSuccess() ? "CAPTURED" : res.getErrorMessage());
        if (res.isSuccess()) {
            outboxPublisher.publishPaymentCaptured(payment);
        } else {
            outboxPublisher.publishPaymentFailed(payment, res.getErrorMessage());
        }
        return payment;
    }

    @Transactional
    public Refund createRefund(UUID paymentId, BigDecimal amount, String currency, String idempotencyKey, String reason) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (idempotencyKey != null) {
            var existing = refundRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        GatewayResponse res = "stripe".equalsIgnoreCase(payment.getGateway())
            ? stripeGateway.refund(payment.getGatewayPaymentId(), amount, currency, idempotencyKey)
            : payPalGateway.refund(payment.getGatewayCaptureId() != null ? payment.getGatewayCaptureId() : payment.getGatewayPaymentId(), amount, currency, idempotencyKey);
        Refund refund = Refund.builder()
            .paymentId(paymentId)
            .idempotencyKey(idempotencyKey)
            .amount(amount)
            .currency(currency)
            .gatewayRefundId(res.getGatewayPaymentId())
            .status(res.isSuccess() ? "COMPLETED" : "FAILED")
            .reason(reason)
            .build();
        refund = refundRepository.save(refund);
        auditService.log("REFUND_CREATE", "refund", refund.getId().toString(), payment.getAccountId(), res.isSuccess() ? "COMPLETED" : res.getErrorMessage());
        if (res.isSuccess()) {
            outboxPublisher.publishRefundCompleted(refund, payment);
        } else {
            outboxPublisher.publishRefundFailed(refund, payment, res.getErrorMessage());
        }
        return refund;
    }
}

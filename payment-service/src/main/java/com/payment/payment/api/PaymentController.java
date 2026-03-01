package com.payment.payment.api;

import com.payment.payment.domain.Payment;
import com.payment.payment.domain.Refund;
import com.payment.payment.service.PaymentOrchestrationService;
import com.payment.payment.service.PaymentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment and refund APIs")
public class PaymentController {

    private final PaymentOrchestrationService orchestrationService;
    private final PaymentQueryService queryService;

    @PostMapping("/payments")
    @Operation(summary = "Create payment", description = "Authorize and capture payment. Use Idempotency-Key header for safe retries.")
    public ResponseEntity<PaymentDto> createPayment(
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody CreatePaymentRequest request
    ) {
        Payment payment = orchestrationService.createPayment(
            request.accountId(),
            request.paymentMethodId(),
            request.gateway(),
            request.paymentMethodGatewayId(),
            request.amount(),
            request.currency(),
            idempotencyKey,
            request.description()
        );
        return ResponseEntity.ok(PaymentDto.from(payment));
    }

    @PostMapping("/payments/{paymentId}/refunds")
    @Operation(summary = "Create refund", description = "Full or partial refund. Use Idempotency-Key for safe retries.")
    public ResponseEntity<RefundDto> createRefund(
        @PathVariable UUID paymentId,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody CreateRefundRequest request
    ) {
        Refund refund = orchestrationService.createRefund(
            paymentId,
            request.amount(),
            request.currency(),
            idempotencyKey,
            request.reason()
        );
        return ResponseEntity.ok(RefundDto.from(refund));
    }

    @GetMapping("/payments")
    @Operation(summary = "List my payments")
    public List<PaymentDto> listPayments(
        @RequestHeader("X-User-Id") String userId,
        @RequestParam UUID accountId,
        @RequestParam(defaultValue = "20") int size
    ) {
        return queryService.findByAccountId(accountId, size).stream().map(PaymentDto::from).toList();
    }

    @GetMapping("/payments/{id}")
    @Operation(summary = "Get payment by ID")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable UUID id) {
        return queryService.findById(id).map(PaymentDto::from).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    public record CreatePaymentRequest(
        UUID accountId,
        UUID paymentMethodId,
        String gateway,
        String paymentMethodGatewayId,
        java.math.BigDecimal amount,
        String currency,
        String description
    ) {}

    public record CreateRefundRequest(java.math.BigDecimal amount, String currency, String reason) {}

    public record PaymentDto(
        UUID id, UUID accountId, String gateway, String status, java.math.BigDecimal amount, String currency,
        String gatewayPaymentId, String createdAt
    ) {
        static PaymentDto from(Payment p) {
            return new PaymentDto(p.getId(), p.getAccountId(), p.getGateway(), p.getStatus(), p.getAmount(), p.getCurrency(),
                p.getGatewayPaymentId(), p.getCreatedAt().toString());
        }
    }

    public record RefundDto(UUID id, UUID paymentId, java.math.BigDecimal amount, String currency, String status, String createdAt) {
        static RefundDto from(Refund r) {
            return new RefundDto(r.getId(), r.getPaymentId(), r.getAmount(), r.getCurrency(), r.getStatus(), r.getCreatedAt().toString());
        }
    }
}

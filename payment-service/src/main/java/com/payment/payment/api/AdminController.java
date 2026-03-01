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

/**
 * Admin/support API: look up payments and trigger refunds.
 * In production, secure with role-based access (e.g. ADMIN only).
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Support and admin operations")
public class AdminController {

    private final PaymentQueryService queryService;
    private final PaymentOrchestrationService orchestrationService;

    @GetMapping("/payments/{id}")
    @Operation(summary = "Get payment by ID (admin)")
    public ResponseEntity<PaymentController.PaymentDto> getPayment(@PathVariable UUID id) {
        return queryService.findById(id)
            .map(PaymentController.PaymentDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/payments")
    @Operation(summary = "Search payments by account (admin)")
    public List<PaymentController.PaymentDto> searchByAccount(@RequestParam UUID accountId, @RequestParam(defaultValue = "50") int size) {
        return queryService.findByAccountId(accountId, size).stream()
            .map(PaymentController.PaymentDto::from)
            .toList();
    }

    @PostMapping("/payments/{paymentId}/refunds")
    @Operation(summary = "Trigger refund (admin)")
    public ResponseEntity<PaymentController.RefundDto> adminRefund(
        @PathVariable UUID paymentId,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @Valid @RequestBody PaymentController.CreateRefundRequest request
    ) {
        Refund refund = orchestrationService.createRefund(
            paymentId,
            request.amount(),
            request.currency(),
            idempotencyKey,
            request.reason()
        );
        return ResponseEntity.ok(PaymentController.RefundDto.from(refund));
    }
}

package com.payment.payment.gateway;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripeGateway {

    @Value("${app.stripe.secret-key:}")
    private String secretKey;


    public GatewayResponse authorizeAndCapture(GatewayRequest request) {
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("Stripe secret key not set; simulating success");
            return GatewayResponse.builder()
                .success(true)
                .gatewayPaymentId("pi_sim_" + request.getIdempotencyKey())
                .gatewayCaptureId("pi_sim_" + request.getIdempotencyKey())
                .status("succeeded")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();
        }
        try {
            com.stripe.Stripe.apiKey = secretKey;
            long amountCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            Map<String, Object> params = new HashMap<>();
            params.put("amount", amountCents);
            params.put("currency", request.getCurrency().toLowerCase());
            params.put("payment_method", request.getPaymentMethodId());
            params.put("confirm", true);
            params.put("automatic_payment_methods", Map.of("enabled", true, "allow_redirects", "never"));
            if (request.getIdempotencyKey() != null) {
                params.put("idempotency_key", request.getIdempotencyKey());
            }
            PaymentIntent intent = PaymentIntent.create(params);
            String status = intent.getStatus();
            boolean success = "succeeded".equals(status);
            return GatewayResponse.builder()
                .success(success)
                .gatewayPaymentId(intent.getId())
                .gatewayCaptureId(success ? intent.getLatestCharge() : null)
                .status(status)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .errorMessage(success ? null : intent.getLastPaymentError() != null ? intent.getLastPaymentError().getMessage() : status)
                .build();
        } catch (StripeException e) {
            log.error("Stripe error", e);
            return GatewayResponse.builder()
                .success(false)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .errorMessage(e.getMessage())
                .build();
        }
    }

    public GatewayResponse refund(String gatewayPaymentId, BigDecimal amount, String currency, String idempotencyKey) {
        if (secretKey == null || secretKey.isBlank()) {
            return GatewayResponse.builder()
                .success(true)
                .gatewayPaymentId("re_sim_" + idempotencyKey)
                .status("succeeded")
                .amount(amount)
                .currency(currency)
                .build();
        }
        try {
            com.stripe.Stripe.apiKey = secretKey;
            Map<String, Object> params = new HashMap<>();
            params.put("payment_intent", gatewayPaymentId);
            params.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
            if (idempotencyKey != null) params.put("idempotency_key", idempotencyKey);
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(params);
            return GatewayResponse.builder()
                .success("succeeded".equals(refund.getStatus()) || "pending".equals(refund.getStatus()))
                .gatewayPaymentId(refund.getId())
                .status(refund.getStatus())
                .amount(amount)
                .currency(currency)
                .build();
        } catch (StripeException e) {
            log.error("Stripe refund error", e);
            return GatewayResponse.builder()
                .success(false)
                .amount(amount)
                .currency(currency)
                .errorMessage(e.getMessage())
                .build();
        }
    }
}

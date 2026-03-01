package com.payment.payment.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * PayPal integration placeholder.
 * In production: use PayPal SDK (e.g. orders v2 capture) with client credentials and idempotency.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayPalGateway {

    @Value("${app.paypal.client-id:}")
    private String clientId;

    public GatewayResponse authorizeAndCapture(GatewayRequest request) {
        if (clientId == null || clientId.isBlank()) {
            log.warn("PayPal client-id not set; simulating success");
            return GatewayResponse.builder()
                .success(true)
                .gatewayPaymentId("paypal_sim_" + request.getIdempotencyKey())
                .gatewayCaptureId("paypal_sim_" + request.getIdempotencyKey())
                .status("COMPLETED")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();
        }
        // TODO: call PayPal Orders API capture with request.getPaymentMethodId() as order ID or payment token
        log.warn("PayPal capture not implemented; returning simulated success");
        return GatewayResponse.builder()
            .success(true)
            .gatewayPaymentId("paypal_" + request.getIdempotencyKey())
            .gatewayCaptureId("paypal_" + request.getIdempotencyKey())
            .status("COMPLETED")
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .build();
    }

    public GatewayResponse refund(String gatewayCaptureId, java.math.BigDecimal amount, String currency, String idempotencyKey) {
        if (clientId == null || clientId.isBlank()) {
            return GatewayResponse.builder()
                .success(true)
                .gatewayPaymentId("paypal_refund_sim_" + idempotencyKey)
                .status("COMPLETED")
                .amount(amount)
                .currency(currency)
                .build();
        }
        // TODO: PayPal refund API
        return GatewayResponse.builder()
            .success(true)
            .gatewayPaymentId("paypal_refund_" + idempotencyKey)
            .status("COMPLETED")
            .amount(amount)
            .currency(currency)
            .build();
    }
}

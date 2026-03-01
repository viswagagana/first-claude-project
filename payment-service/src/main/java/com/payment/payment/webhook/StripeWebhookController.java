package com.payment.payment.webhook;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe webhook endpoint. Verify signature using STRIPE_WEBHOOK_SECRET.
 * In production, persist webhook events and process asynchronously to avoid timeouts.
 */
@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    @Value("${app.stripe.webhook-secret:}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handle(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sig) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Stripe webhook secret not set; skipping verification");
            return ResponseEntity.ok().body("ok");
        }
        try {
            Webhook.constructEvent(payload, sig, webhookSecret);
            // TODO: dispatch by event type (payment_intent.succeeded, charge.refunded, etc.) and update local state / outbox
            log.info("Stripe webhook verified");
            return ResponseEntity.ok().body("ok");
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature invalid");
            return ResponseEntity.status(400).body("Invalid signature");
        }
    }
}

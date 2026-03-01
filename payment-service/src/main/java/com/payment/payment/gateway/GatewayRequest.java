package com.payment.payment.gateway;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class GatewayRequest {
    UUID accountId;
    String paymentMethodId;  // gateway-specific (e.g. Stripe pm_xxx)
    BigDecimal amount;
    String currency;
    String idempotencyKey;
    String description;
}

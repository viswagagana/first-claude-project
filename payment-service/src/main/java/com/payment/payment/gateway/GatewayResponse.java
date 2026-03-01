package com.payment.payment.gateway;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class GatewayResponse {
    boolean success;
    String gatewayPaymentId;
    String gatewayCaptureId;
    String status;
    BigDecimal amount;
    String currency;
    String errorMessage;
}

package com.payment.contracts.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RefundCompletedEvent extends PaymentEvent {
    private String refundId;
    private String paymentId;
    private BigDecimal refundAmount;
    private String gateway;
    private String gatewayRefundId;
}

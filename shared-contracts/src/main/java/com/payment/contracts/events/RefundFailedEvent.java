package com.payment.contracts.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RefundFailedEvent extends PaymentEvent {
    private String refundId;
    private String reason;
    private String gateway;
}

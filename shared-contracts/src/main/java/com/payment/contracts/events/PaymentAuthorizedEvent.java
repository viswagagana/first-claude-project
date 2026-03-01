package com.payment.contracts.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizedEvent extends PaymentEvent {
    private String gateway;
    private String gatewayReference;
}

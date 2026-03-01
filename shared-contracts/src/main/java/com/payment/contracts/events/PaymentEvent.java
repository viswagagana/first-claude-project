package com.payment.contracts.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Value(name = "PaymentAuthorized", value = PaymentAuthorizedEvent.class),
    @JsonSubTypes.Value(name = "PaymentCaptured", value = PaymentCapturedEvent.class),
    @JsonSubTypes.Value(name = "PaymentFailed", value = PaymentFailedEvent.class),
    @JsonSubTypes.Value(name = "RefundCompleted", value = RefundCompletedEvent.class),
    @JsonSubTypes.Value(name = "RefundFailed", value = RefundFailedEvent.class)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PaymentEvent {
    private String eventId;
    private Instant timestamp;
    private String paymentId;
    private String accountId;
    private String currency;
    private BigDecimal amount;
}

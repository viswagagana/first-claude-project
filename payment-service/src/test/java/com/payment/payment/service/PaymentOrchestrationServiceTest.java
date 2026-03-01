package com.payment.payment.service;

import com.payment.payment.domain.Payment;
import com.payment.payment.gateway.GatewayResponse;
import com.payment.payment.gateway.PayPalGateway;
import com.payment.payment.gateway.StripeGateway;
import com.payment.payment.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOrchestrationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private IdempotencyRepository idempotencyRepository;
    @Mock
    private StripeGateway stripeGateway;
    @Mock
    private PayPalGateway payPalGateway;
    @Mock
    private OutboxPublisher outboxPublisher;
    @Mock
    private PaymentAuditService auditService;

    private PaymentOrchestrationService service;

    @BeforeEach
    void setUp() {
        service = new PaymentOrchestrationService(
            paymentRepository,
            refundRepository,
            idempotencyRepository,
            stripeGateway,
            payPalGateway,
            outboxPublisher,
            auditService
        );
    }

    @Test
    void createPayment_usesStripeAndReturnsPayment() {
        UUID accountId = UUID.randomUUID();
        UUID pmId = UUID.randomUUID();
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(stripeGateway.authorizeAndCapture(any())).thenReturn(GatewayResponse.builder()
            .success(true)
            .gatewayPaymentId("pi_123")
            .gatewayCaptureId("ch_123")
            .status("succeeded")
            .amount(new BigDecimal("10.00"))
            .currency("USD")
            .build());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = service.createPayment(
            accountId,
            pmId,
            "stripe",
            "pm_xxx",
            new BigDecimal("10.00"),
            "USD",
            null,
            "Test"
        );

        assertThat(payment).isNotNull();
        assertThat(payment.getGateway()).isEqualTo("stripe");
        assertThat(payment.getStatus()).isEqualTo("CAPTURED");
        assertThat(payment.getAmount()).isEqualByComparingTo("10.00");
    }
}

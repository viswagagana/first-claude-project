package com.payment.payment.repository;

import com.payment.payment.domain.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends org.springframework.data.jpa.repository.JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByAccountIdOrderByCreatedAtDesc(UUID accountId, org.springframework.data.domain.Pageable pageable);

    List<Payment> findByGateway(String gateway);
}

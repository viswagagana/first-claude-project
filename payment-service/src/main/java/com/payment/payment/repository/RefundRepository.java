package com.payment.payment.repository;

import com.payment.payment.domain.Refund;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRepository extends org.springframework.data.jpa.repository.JpaRepository<Refund, UUID> {

    Optional<Refund> findByIdempotencyKey(String idempotencyKey);

    List<Refund> findByPaymentId(UUID paymentId);
}

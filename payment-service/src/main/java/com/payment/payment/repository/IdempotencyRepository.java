package com.payment.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IdempotencyRepository extends JpaRepository<com.payment.payment.domain.IdempotencyRecord, String> {
}

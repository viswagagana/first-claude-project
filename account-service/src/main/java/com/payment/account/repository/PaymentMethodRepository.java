package com.payment.account.repository;

import com.payment.account.domain.PaymentMethod;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodRepository extends org.springframework.data.jpa.repository.JpaRepository<PaymentMethod, UUID> {

    List<PaymentMethod> findByAccountId(UUID accountId);
}

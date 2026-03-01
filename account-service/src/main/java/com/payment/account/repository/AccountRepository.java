package com.payment.account.repository;

import com.payment.account.domain.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends org.springframework.data.jpa.repository.JpaRepository<Account, UUID> {

    Optional<Account> findByUserId(UUID userId);
}

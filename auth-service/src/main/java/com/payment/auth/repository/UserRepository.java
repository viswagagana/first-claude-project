package com.payment.auth.repository;

import com.payment.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends org.springframework.data.jpa.repository.JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

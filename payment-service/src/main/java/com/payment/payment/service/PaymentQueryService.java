package com.payment.payment.service;

import com.payment.payment.domain.Payment;
import com.payment.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> findByAccountId(UUID accountId, int size) {
        return paymentRepository.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, size));
    }
}

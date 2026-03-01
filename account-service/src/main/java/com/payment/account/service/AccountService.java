package com.payment.account.service;

import com.payment.account.domain.Account;
import com.payment.account.domain.PaymentMethod;
import com.payment.account.repository.AccountRepository;
import com.payment.account.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    public Optional<Account> findByUserId(UUID userId) {
        return accountRepository.findByUserId(userId);
    }

    @Transactional
    public Account getOrCreateAccount(UUID userId) {
        return accountRepository.findByUserId(userId)
            .orElseGet(() -> {
                Account a = Account.builder().userId(userId).status("ACTIVE").build();
                return accountRepository.save(a);
            });
    }

    @Transactional
    public PaymentMethod addPaymentMethod(UUID accountId, String gateway, String gatewayId, String type, String displayName, Boolean isDefault) {
        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("Account not found");
        }
        if (Boolean.TRUE.equals(isDefault)) {
            paymentMethodRepository.findByAccountId(accountId).forEach(pm -> pm.setIsDefault(false));
            paymentMethodRepository.flush();
        }
        PaymentMethod pm = PaymentMethod.builder()
            .accountId(accountId)
            .gateway(gateway)
            .gatewayId(gatewayId)
            .type(type != null ? type : "card")
            .displayName(displayName)
            .isDefault(Boolean.TRUE.equals(isDefault))
            .build();
        return paymentMethodRepository.save(pm);
    }
}

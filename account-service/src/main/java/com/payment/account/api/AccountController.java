package com.payment.account.api;

import com.payment.account.domain.Account;
import com.payment.account.domain.PaymentMethod;
import com.payment.account.repository.AccountRepository;
import com.payment.account.repository.PaymentMethodRepository;
import com.payment.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account and payment method management")
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @GetMapping("/accounts/me")
    @Operation(summary = "Get my account", description = "Returns account for the current user (user_id from context)")
    public ResponseEntity<AccountDto> getMyAccount(@RequestHeader("X-User-Id") String userId) {
        return accountService.findByUserId(UUID.fromString(userId))
            .map(AccountDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/accounts")
    @Operation(summary = "Create account", description = "Create account for user if not exists")
    public ResponseEntity<AccountDto> createAccount(@RequestHeader("X-User-Id") String userId) {
        Account account = accountService.getOrCreateAccount(UUID.fromString(userId));
        return ResponseEntity.ok(AccountDto.from(account));
    }

    @GetMapping("/accounts/{accountId}/payment-methods")
    @Operation(summary = "List payment methods")
    public List<PaymentMethodDto> listPaymentMethods(@PathVariable UUID accountId) {
        return paymentMethodRepository.findByAccountId(accountId)
            .stream()
            .map(PaymentMethodDto::from)
            .toList();
    }

    @PostMapping("/accounts/{accountId}/payment-methods")
    @Operation(summary = "Add payment method", description = "Register a gateway payment method (e.g. Stripe pm_xxx)")
    public ResponseEntity<PaymentMethodDto> addPaymentMethod(
        @PathVariable UUID accountId,
        @RequestBody AddPaymentMethodRequest request
    ) {
        PaymentMethod pm = accountService.addPaymentMethod(accountId, request.gateway(), request.gatewayId(), request.type(), request.displayName(), request.isDefault());
        return ResponseEntity.ok(PaymentMethodDto.from(pm));
    }

    public record AccountDto(UUID id, UUID userId, String status) {
        static AccountDto from(Account a) {
            return new AccountDto(a.getId(), a.getUserId(), a.getStatus());
        }
    }

    public record PaymentMethodDto(UUID id, UUID accountId, String type, String gateway, String displayName, Boolean isDefault) {
        static PaymentMethodDto from(PaymentMethod p) {
            return new PaymentMethodDto(p.getId(), p.getAccountId(), p.getType(), p.getGateway(), p.getDisplayName(), p.getIsDefault());
        }
    }

    public record AddPaymentMethodRequest(String gateway, String gatewayId, String type, String displayName, Boolean isDefault) {}
}

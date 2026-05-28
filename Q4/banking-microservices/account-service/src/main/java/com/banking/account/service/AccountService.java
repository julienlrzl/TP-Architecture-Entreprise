package com.banking.account.service;

import com.banking.account.dto.AccountRequest;
import com.banking.account.dto.AccountResponse;
import com.banking.account.dto.BalanceUpdateRequest;
import com.banking.account.model.Account;
import com.banking.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Account.AccountType type;
        try {
            type = Account.AccountType.valueOf(request.getAccountType().toUpperCase());
        } catch (IllegalArgumentException e) {
            type = Account.AccountType.CHECKING;
        }

        BigDecimal initialBalance = request.getInitialBalance() != null
                ? request.getInitialBalance()
                : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .userId(request.getUserId())
                .ownerName(request.getOwnerName())
                .balance(initialBalance)
                .accountType(type)
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    public AccountResponse getAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found: " + id));
        return toResponse(account);
    }

    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse updateBalance(Long accountId, BalanceUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        BigDecimal amount = request.getAmount();
        String operation = request.getOperation().toUpperCase();

        switch (operation) {
            case "DEPOSIT":
                account.setBalance(account.getBalance().add(amount));
                break;
            case "WITHDRAW":
                if (account.getBalance().compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient funds. Balance: "
                            + account.getBalance() + ", Requested: " + amount);
                }
                account.setBalance(account.getBalance().subtract(amount));
                break;
            default:
                throw new RuntimeException("Unknown operation: " + operation);
        }

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "ACC" + String.format("%010d", new Random().nextLong(9_999_999_999L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .ownerName(account.getOwnerName())
                .balance(account.getBalance())
                .accountType(account.getAccountType().name())
                .createdAt(account.getCreatedAt())
                .active(account.isActive())
                .build();
    }
}

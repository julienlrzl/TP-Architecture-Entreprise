package com.banking.transaction.service;

import com.banking.transaction.client.AccountClient;
import com.banking.transaction.client.NotificationClient;
import com.banking.transaction.dto.*;
import com.banking.transaction.model.Transaction;
import com.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    @Transactional
    public TransactionResponse deposit(TransactionRequest request) {
        Long accountId = request.getAccountId();
        BigDecimal amount = request.getAmount();

        AccountDto account = accountClient.getAccount(accountId);
        if (!account.isActive()) {
            throw new RuntimeException("Account is not active: " + accountId);
        }

        accountClient.updateBalance(accountId, new BalanceUpdateRequest(amount, "DEPOSIT"));

        Transaction transaction = Transaction.builder()
                .sourceAccountId(accountId)
                .amount(amount)
                .type(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Deposit")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        sendNotification("DEPOSIT", accountId, saved.getId(),
                String.format("Deposit of %.2f to account %s", amount, account.getAccountNumber()));

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse withdraw(TransactionRequest request) {
        Long accountId = request.getAccountId();
        BigDecimal amount = request.getAmount();

        AccountDto account = accountClient.getAccount(accountId);
        if (!account.isActive()) {
            throw new RuntimeException("Account is not active: " + accountId);
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds. Balance: "
                    + account.getBalance() + ", Requested: " + amount);
        }

        accountClient.updateBalance(accountId, new BalanceUpdateRequest(amount, "WITHDRAW"));

        Transaction transaction = Transaction.builder()
                .sourceAccountId(accountId)
                .amount(amount)
                .type(Transaction.TransactionType.WITHDRAWAL)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Withdrawal")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        sendNotification("WITHDRAWAL", accountId, saved.getId(),
                String.format("Withdrawal of %.2f from account %s", amount, account.getAccountNumber()));

        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse transfer(TransactionRequest request) {
        Long sourceId = request.getSourceAccountId();
        Long targetId = request.getTargetAccountId();
        BigDecimal amount = request.getAmount();

        AccountDto source = accountClient.getAccount(sourceId);
        AccountDto target = accountClient.getAccount(targetId);

        if (!source.isActive()) {
            throw new RuntimeException("Source account is not active: " + sourceId);
        }
        if (!target.isActive()) {
            throw new RuntimeException("Target account is not active: " + targetId);
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds. Balance: "
                    + source.getBalance() + ", Requested: " + amount);
        }

        accountClient.updateBalance(sourceId, new BalanceUpdateRequest(amount, "WITHDRAW"));
        accountClient.updateBalance(targetId, new BalanceUpdateRequest(amount, "DEPOSIT"));

        Transaction transaction = Transaction.builder()
                .sourceAccountId(sourceId)
                .targetAccountId(targetId)
                .amount(amount)
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description(request.getDescription() != null ? request.getDescription() : "Transfer")
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        sendNotification("TRANSFER_DEBIT", sourceId, saved.getId(),
                String.format("Transfer of %.2f from %s to %s",
                        amount, source.getAccountNumber(), target.getAccountNumber()));
        sendNotification("TRANSFER_CREDIT", targetId, saved.getId(),
                String.format("Transfer of %.2f received from %s to %s",
                        amount, source.getAccountNumber(), target.getAccountNumber()));

        return toResponse(saved);
    }

    public List<TransactionResponse> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void sendNotification(String eventType, Long accountId, Long transactionId, String message) {
        try {
            notificationClient.sendNotification(
                    new NotificationRequest(eventType, message, accountId, transactionId));
        } catch (Exception e) {
            log.warn("Failed to send notification for transaction {}: {}", transactionId, e.getMessage());
        }
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .sourceAccountId(transaction.getSourceAccountId())
                .targetAccountId(transaction.getTargetAccountId())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

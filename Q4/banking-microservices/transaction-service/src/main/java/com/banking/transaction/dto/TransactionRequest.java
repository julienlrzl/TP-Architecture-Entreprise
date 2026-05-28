package com.banking.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private Long accountId;
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String description;
}

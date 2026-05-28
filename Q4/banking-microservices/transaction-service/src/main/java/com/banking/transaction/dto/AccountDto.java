package com.banking.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String accountNumber;
    private Long userId;
    private String ownerName;
    private BigDecimal balance;
    private String accountType;
    private LocalDateTime createdAt;
    private boolean active;
}

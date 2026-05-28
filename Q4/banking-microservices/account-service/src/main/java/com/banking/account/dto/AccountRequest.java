package com.banking.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {
    private Long userId;
    private String ownerName;
    private BigDecimal initialBalance;
    private String accountType;
}

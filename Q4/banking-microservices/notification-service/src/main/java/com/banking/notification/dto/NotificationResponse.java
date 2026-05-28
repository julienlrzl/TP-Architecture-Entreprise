package com.banking.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String eventType;
    private String message;
    private Long accountId;
    private Long transactionId;
    private LocalDateTime createdAt;
    private String status;
}

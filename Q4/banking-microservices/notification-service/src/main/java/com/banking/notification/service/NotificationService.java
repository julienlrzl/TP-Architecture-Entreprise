package com.banking.notification.service;

import com.banking.notification.dto.NotificationRequest;
import com.banking.notification.dto.NotificationResponse;
import com.banking.notification.model.Notification;
import com.banking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("[EVENT] {} | Account: {} | Transaction: {} | {}",
                request.getEventType(), request.getAccountId(),
                request.getTransactionId(), request.getMessage());

        Notification notification = Notification.builder()
                .eventType(request.getEventType())
                .message(request.getMessage())
                .accountId(request.getAccountId())
                .transactionId(request.getTransactionId())
                .createdAt(LocalDateTime.now())
                .status("DELIVERED")
                .build();

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getNotificationsByAccount(Long accountId) {
        return notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .eventType(notification.getEventType())
                .message(notification.getMessage())
                .accountId(notification.getAccountId())
                .transactionId(notification.getTransactionId())
                .createdAt(notification.getCreatedAt())
                .status(notification.getStatus())
                .build();
    }
}

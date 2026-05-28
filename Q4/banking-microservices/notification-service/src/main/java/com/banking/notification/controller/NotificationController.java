package com.banking.notification.controller;

import com.banking.notification.dto.NotificationRequest;
import com.banking.notification.dto.NotificationResponse;
import com.banking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<NotificationResponse>> getByAccount(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(notificationService.getNotificationsByAccount(accountId));
    }
}

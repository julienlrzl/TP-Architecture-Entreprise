package com.banking.notification.repository;

import com.banking.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}

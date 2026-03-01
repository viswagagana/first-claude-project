package com.payment.notification.repository;

import com.payment.notification.domain.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    boolean existsByEventId(String eventId);

    List<Notification> findByAccountIdOrderByCreatedAtDesc(String accountId, Pageable pageable);
}

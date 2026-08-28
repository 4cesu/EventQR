package com.thedavelopers.eventqr.features.notifications.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thedavelopers.eventqr.features.notifications.model.entity.Notification;
import com.thedavelopers.eventqr.shared.constants.NotificationStatus;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientUserId(UUID recipientUserId);

    List<Notification> findByEventId(UUID eventId);

    long countByRecipientUserIdAndStatusNot(UUID recipientUserId, NotificationStatus status);
}
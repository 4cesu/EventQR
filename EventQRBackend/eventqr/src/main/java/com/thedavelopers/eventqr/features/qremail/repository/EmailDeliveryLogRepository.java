package com.thedavelopers.eventqr.features.qremail.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thedavelopers.eventqr.features.qremail.model.entity.EmailDeliveryLog;

public interface EmailDeliveryLogRepository extends JpaRepository<EmailDeliveryLog, UUID> {

    List<EmailDeliveryLog> findByRegistrationIdOrderByAttemptedAtDesc(UUID registrationId);

    List<EmailDeliveryLog> findByQrCredentialIdOrderByAttemptedAtDesc(UUID qrCredentialId);
}

package com.thedavelopers.eventqr.features.qremail.model.entity;

import java.time.Instant;
import java.util.UUID;

import com.thedavelopers.eventqr.features.qremail.model.EmailDeliveryStatus;
import com.thedavelopers.eventqr.shared.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "email_delivery_logs")
public class EmailDeliveryLog extends BaseEntity {

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private UUID registrationId;

    @Column(nullable = false)
    private UUID qrCredentialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailDeliveryStatus status;

    @Column(nullable = false)
    private Instant attemptedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}

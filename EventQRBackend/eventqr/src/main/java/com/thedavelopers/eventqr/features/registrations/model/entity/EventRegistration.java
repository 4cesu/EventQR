package com.thedavelopers.eventqr.features.registrations.model.entity;

import java.time.Instant;
import java.util.UUID;

import com.thedavelopers.eventqr.shared.constants.RegistrationStatus;
import com.thedavelopers.eventqr.shared.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Getter
@Setter
@Entity
@Table(name = "event_registrations")
public class EventRegistration extends BaseEntity {

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID attendeeUserId;

    @Column(nullable = false)
    private String attendeeEmail;

    @Column(nullable = false)
    private String attendeeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    /**
     * Per-event sequence number assigned by DB trigger trg_assign_registration_number (V13).
     * The database owns this value: Hibernate omits it from INSERT/UPDATE and re-reads it
     * after insert so trigger-assigned numbers are visible without a manual refresh.
     */
    @Generated(event = EventType.INSERT)
    @Column(name = "registration_number", insertable = false, updatable = false)
    private Integer registrationNumber;

    private UUID qrCredentialId;

    private Instant registeredAt;

    private Instant enteredAt;

    private Instant exitedAt;

    private Instant attendedAt;

    @Column(nullable = false)
    private Integer pointsEarned = 0;
}

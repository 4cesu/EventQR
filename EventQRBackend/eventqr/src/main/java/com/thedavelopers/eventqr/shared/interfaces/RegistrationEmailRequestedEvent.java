package com.thedavelopers.eventqr.shared.interfaces;

import java.util.UUID;

/**
 * Published by the registration flow after the registration transaction commits.
 * The QR email delivery is handled asynchronously by an
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} so long email/gateway
 * work (with retry sleeps) never holds a JDBC connection open inside the
 * registration transaction.
 */
public record RegistrationEmailRequestedEvent(UUID registrationId) {
}
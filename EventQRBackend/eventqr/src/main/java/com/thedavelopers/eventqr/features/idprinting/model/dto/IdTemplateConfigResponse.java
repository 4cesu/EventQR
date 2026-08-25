package com.thedavelopers.eventqr.features.idprinting.model.dto;

import java.util.List;
import java.util.UUID;

/**
 * Response payload describing the current ID display configuration (SDD Module 3.7).
 *
 * {@code visibleFields} is the organizer-selected subset of ATTENDEE_ID, ROLE, EVENT_NAME,
 * EVENT_DATE. {@code lockedFields} is always ["QR_CODE", "ATTENDEE_NAME"] — constant,
 * server-injected, and echoed back so renderers can rely on one shape.
 */
public record IdTemplateConfigResponse(UUID eventId, List<String> visibleFields, List<String> lockedFields) {
}

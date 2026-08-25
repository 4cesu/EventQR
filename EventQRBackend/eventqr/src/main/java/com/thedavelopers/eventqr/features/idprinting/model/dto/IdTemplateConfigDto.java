package com.thedavelopers.eventqr.features.idprinting.model.dto;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for SDD Module 3.7 "Configure ID Display Fields".
 *
 * Only {@code visibleFields} is honored; the {@code eventId} path parameter is always the
 * source of truth and any body-supplied eventId is ignored.
 *
 * SDD 3.7 deviation note (capstone defense): SRS UC-22 describes logo upload, color editing,
 * and predefined template selection, but SDD 3.7 explicitly overrides it — "organizer cannot
 * edit the ID layout, design, colors, logo, or visual format." This DTO therefore carries
 * field visibility only: no style/template/logo fields are accepted, and lockedFields is NOT
 * accepted from the client — the server always injects ["QR_CODE", "ATTENDEE_NAME"].
 */
public record IdTemplateConfigDto(UUID eventId, List<String> visibleFields) {
}

package com.thedavelopers.eventqr.features.idprinting.model.dto

// SDD 3.7 deviation note (capstone defense): SRS UC-22 describes logo upload, color editing,
// and predefined template selection; SDD 3.7 explicitly overrides it — "organizer cannot edit
// the ID layout, design, colors, logo, or visual format." Only field visibility toggling exists
// here: no color picker, no logo upload, no template style selector.
data class IdTemplateConfigRequest(
    val visibleFields: List<String> = emptyList(),
)

// ATTENDEE_ID renders event_registrations.registration_number (per-event sequence assigned by
// a DB trigger, migration V13). Replaces the earlier user_profiles.id UUID source.
data class IdTemplateConfigResponse(
    val eventId: String? = null,
    val visibleFields: List<String?> = emptyList(),
    val lockedFields: List<String?> = emptyList(),
)

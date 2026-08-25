package com.thedavelopers.eventqr.features.idprinting.model.dto

// SDD 3.7 deviation note (capstone defense): SRS UC-22 describes logo upload, color editing,
// and predefined template selection; SDD 3.7 explicitly overrides it — "organizer cannot edit
// the ID layout, design, colors, logo, or visual format." Only field visibility toggling exists
// here: no color picker, no logo upload, no template style selector.
data class IdTemplateConfigRequest(
    val visibleFields: List<String> = emptyList(),
)

// Known limitation (scope-tracked decision): ATTENDEE_ID renders user_profiles.id (UUID,
// 36 chars) because no other identifier column exists without deviating from the SDD.
data class IdTemplateConfigResponse(
    val eventId: String? = null,
    val visibleFields: List<String?> = emptyList(),
    val lockedFields: List<String?> = emptyList(),
)

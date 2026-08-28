package com.thedavelopers.eventqr.features.idprinting.model.dto

import java.time.Instant
import java.util.UUID

/** Request for printing multiple ID cards in one batch (one log row per entry). */
data class IdBatchPrintRequest(
    val attendeeUserIds: List<UUID>,
    val reprint: Boolean,
)

data class IdPrintResponse(
    val printLogId: UUID,
    val eventId: UUID,
    val attendeeUserId: UUID,
    val registrationId: UUID,
    val qrCredentialId: UUID,
    val templateId: UUID,
    val reprint: Boolean,
    val success: Boolean,
    val message: String,
    val printedAt: Instant? = null,
)

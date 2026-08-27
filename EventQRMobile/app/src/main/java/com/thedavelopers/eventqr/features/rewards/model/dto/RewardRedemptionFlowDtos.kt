package com.thedavelopers.eventqr.features.rewards.model.dto

import com.thedavelopers.eventqr.core.api.dto.RedemptionStatus
import com.thedavelopers.eventqr.core.api.dto.RegistrationStatus
import java.time.Instant
import java.util.UUID

data class RewardRedemptionScanRequest(
    val eventId: UUID,
    val scanPurposeId: UUID,
    val qrValue: String? = null,
    val shortId: String? = null,
    val staffUserId: UUID? = null,
)

data class RewardRedemptionScanResponse(
    val eventId: UUID,
    val attendeeUserId: UUID,
    val registrationId: UUID,
    val qrCredentialId: UUID,
    val attendeeName: String? = null,
    val attendeeEmail: String? = null,
    val registrationStatus: RegistrationStatus,
    val pointsBalance: Int,
    val scanPurposeId: UUID,
    val redemptionScanLogId: UUID,
    val eligibleRewards: List<RewardResponse> = emptyList(),
)

data class RewardRedemptionGrantRequest(
    val eventId: UUID,
    val attendeeUserId: UUID,
    val rewardId: UUID,
    val staffUserId: UUID? = null,
    val redemptionScanLogId: UUID,
)

data class RewardRedemptionResultResponse(
    val redemptionId: UUID,
    val rewardId: UUID,
    val rewardName: String? = null,
    val status: RedemptionStatus,
    val reason: String? = null,
    val pointsSpent: Int,
    val redeemedAt: Instant? = null,
    val remainingBalance: Int,
)

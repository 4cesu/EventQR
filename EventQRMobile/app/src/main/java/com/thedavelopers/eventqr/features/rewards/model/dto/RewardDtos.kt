package com.thedavelopers.eventqr.features.rewards.model.dto

import com.thedavelopers.eventqr.core.api.dto.RedemptionStatus
import com.thedavelopers.eventqr.core.api.dto.RewardStatus
import java.time.Instant
import java.util.UUID

data class PointBalanceResponse(
    val eventId: UUID,
    val attendeeUserId: UUID,
    val pointsBalance: Int,
)

data class RewardRedemptionRequest(
    val eventId: UUID,
    val attendeeUserId: UUID,
    val rewardId: UUID,
)

data class RewardRedemptionResponse(
    val redemptionId: UUID,
    val eventId: UUID,
    val attendeeUserId: UUID,
    val rewardId: UUID,
    val pointsSpent: Int,
    val status: RedemptionStatus,
    val redeemedAt: Instant? = null,
    val reason: String? = null,
)

data class RewardRequest(
    val eventId: UUID,
    val name: String,
    val pointsRequired: Int,
    val stockQuantity: Int? = null,
    val allowDuplicateClaims: Boolean = false,
)

data class RewardResponse(
    val rewardId: UUID,
    val eventId: UUID,
    val name: String,
    val description: String? = null,
    val pointsRequired: Int,
    val status: RewardStatus,
    val stockQuantity: Int? = null,
    val allowDuplicateClaims: Boolean = false,
)

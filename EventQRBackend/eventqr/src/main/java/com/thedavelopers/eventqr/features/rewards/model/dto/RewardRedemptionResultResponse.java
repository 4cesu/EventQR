package com.thedavelopers.eventqr.features.rewards.model.dto;

import java.time.Instant;
import java.util.UUID;

import com.thedavelopers.eventqr.shared.constants.RedemptionStatus;

public record RewardRedemptionResultResponse(UUID redemptionId,
                                             UUID rewardId,
                                             String rewardName,
                                             RedemptionStatus status,
                                             String reason,
                                             int pointsSpent,
                                             Instant redeemedAt,
                                             int remainingBalance) {

    public boolean approved() {
        return status == RedemptionStatus.REDEEMED;
    }
}

package com.thedavelopers.eventqr.features.rewards.model.dto;

import java.util.List;
import java.util.UUID;

import com.thedavelopers.eventqr.shared.constants.RegistrationStatus;

public record RewardRedemptionScanResponse(UUID eventId,
                                           UUID attendeeUserId,
                                           UUID registrationId,
                                           UUID qrCredentialId,
                                           String attendeeName,
                                           String attendeeEmail,
                                           RegistrationStatus registrationStatus,
                                           int pointsBalance,
                                           UUID scanPurposeId,
                                           UUID redemptionScanLogId,
                                           List<RewardResponse> eligibleRewards) {
}

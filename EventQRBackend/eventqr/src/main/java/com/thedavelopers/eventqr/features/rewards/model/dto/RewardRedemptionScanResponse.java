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
                                           List<RewardResponse> eligibleRewards,
                                           boolean rejected,
                                           String rejectionReason) {

    public RewardRedemptionScanResponse {
        eligibleRewards = eligibleRewards == null ? List.of() : eligibleRewards;
    }

    public RewardRedemptionScanResponse(UUID eventId,
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
        this(eventId, attendeeUserId, registrationId, qrCredentialId, attendeeName, attendeeEmail,
                registrationStatus, pointsBalance, scanPurposeId, redemptionScanLogId, eligibleRewards, false, null);
    }
}

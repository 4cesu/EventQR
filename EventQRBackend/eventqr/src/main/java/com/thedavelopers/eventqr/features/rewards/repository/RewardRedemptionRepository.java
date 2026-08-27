package com.thedavelopers.eventqr.features.rewards.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thedavelopers.eventqr.features.rewards.model.entity.RewardRedemption;
import com.thedavelopers.eventqr.shared.constants.RedemptionStatus;

public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, UUID> {

    Optional<RewardRedemption> findByEventIdAndAttendeeUserIdAndRewardId(UUID eventId, UUID attendeeUserId, UUID rewardId);

    List<RewardRedemption> findByEventId(UUID eventId);

    List<RewardRedemption> findByEventIdAndAttendeeUserId(UUID eventId, UUID attendeeUserId);

    List<RewardRedemption> findByAttendeeUserIdAndRewardIdAndStatus(UUID attendeeUserId, UUID rewardId, RedemptionStatus status);
}
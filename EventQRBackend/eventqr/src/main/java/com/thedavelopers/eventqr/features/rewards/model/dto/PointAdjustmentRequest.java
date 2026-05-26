package com.thedavelopers.eventqr.features.rewards.model.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PointAdjustmentRequest(@NotNull UUID attendeeUserId, @Min(0) int points, String reason) {
}
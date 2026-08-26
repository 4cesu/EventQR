package com.thedavelopers.eventqr.features.transactions.model.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionRequest(@NotNull UUID eventId, @NotNull UUID scanPurposeId, String qrValue,
                                 String shortId, UUID staffUserId, String notes) {

    /** Normalize short ID input: strip "#" prefix and leading zeros, parse to integer. Returns null if invalid. */
    public Integer parsedShortId() {
        if (shortId == null || shortId.isBlank()) return null;
        String cleaned = shortId.trim().replaceFirst("^#+", "").replaceFirst("^0+", "");
        if (cleaned.isEmpty()) return null;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean hasShortId() {
        return parsedShortId() != null;
    }
}
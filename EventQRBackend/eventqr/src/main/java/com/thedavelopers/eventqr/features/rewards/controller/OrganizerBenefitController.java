package com.thedavelopers.eventqr.features.rewards.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.shared.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/organizer/events/{eventId}/benefits")
public class OrganizerBenefitController {

    @GetMapping
    public ResponseEntity<ApiResponse<Void>> list(@PathVariable UUID eventId) {
        return notImplemented("Benefit storage is not wired yet");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@PathVariable UUID eventId) {
        return notImplemented("Benefit storage is not wired yet");
    }

    @PatchMapping("/{benefitId}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable UUID eventId, @PathVariable UUID benefitId) {
        return notImplemented("Benefit storage is not wired yet");
    }

    @DeleteMapping("/{benefitId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID eventId, @PathVariable UUID benefitId) {
        return notImplemented("Benefit storage is not wired yet");
    }

    @GetMapping("/claims")
    public ResponseEntity<ApiResponse<Void>> claims(@PathVariable UUID eventId) {
        return notImplemented("Benefit claim storage is not wired yet");
    }

    @GetMapping("/benefit-claims")
    public ResponseEntity<ApiResponse<Void>> benefitClaims(@PathVariable UUID eventId) {
        return notImplemented("Benefit claim storage is not wired yet");
    }

    private ResponseEntity<ApiResponse<Void>> notImplemented(String message) {
        return ResponseEntity.status(501).body(new ApiResponse<>(false, message, null, java.time.Instant.now()));
    }
}
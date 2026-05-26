package com.thedavelopers.eventqr.features.notifications.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.shared.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/organizer/events/{eventId}/audit-logs")
public class OrganizerAuditController {

    @GetMapping
    public ResponseEntity<ApiResponse<Void>> auditLogs(@PathVariable UUID eventId) {
        return ResponseEntity.status(501).body(new ApiResponse<>(false, "Audit log storage is not wired yet", null, java.time.Instant.now()));
    }
}
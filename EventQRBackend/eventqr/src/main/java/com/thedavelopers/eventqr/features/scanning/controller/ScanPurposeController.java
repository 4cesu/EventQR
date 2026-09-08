package com.thedavelopers.eventqr.features.scanning.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.events.service.EventService;
import com.thedavelopers.eventqr.features.organizer.repository.EventStaffAssignmentRepository;
import com.thedavelopers.eventqr.features.scanning.model.dto.ScanPurposeRequest;
import com.thedavelopers.eventqr.features.scanning.model.dto.ScanPurposeResponse;
import com.thedavelopers.eventqr.features.scanning.service.ScanPurposeService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/scan-purposes")
public class ScanPurposeController {

    private final ScanPurposeService scanPurposeService;
    private final EventService eventService;
    private final JwtService jwtService;
    private final EventStaffAssignmentRepository eventStaffAssignmentRepository;

    public ScanPurposeController(ScanPurposeService scanPurposeService, EventService eventService,
                                 JwtService jwtService, EventStaffAssignmentRepository eventStaffAssignmentRepository) {
        this.scanPurposeService = scanPurposeService;
        this.eventService = eventService;
        this.jwtService = jwtService;
        this.eventStaffAssignmentRepository = eventStaffAssignmentRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScanPurposeResponse>> create(HttpServletRequest request,
                                                                   @Valid @RequestBody ScanPurposeRequest body) {
        requireEventAccess(request, body.eventId());
        return ResponseEntity.ok(ApiResponse.success("Scan purpose created", scanPurposeService.create(body)));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<ScanPurposeResponse>>> findByEvent(HttpServletRequest request,
                                                                              @PathVariable UUID eventId) {
        requireEventAccess(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(scanPurposeService.findByEventId(eventId)));
    }

    /**
     * Mirrors the ownership/role guard used by other organizer-scoped endpoints:
     * admins always pass; an ORGANIZER must own the event; an actively-assigned STAFF
     * member of the event passes. Any other role (e.g. ATTENDEE) is rejected.
     */
    private void requireEventAccess(HttpServletRequest request, UUID eventId) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ORGANIZER) {
            if (eventService.findOne(eventId).organizerUserId().equals(callerId)) {
                return;
            }
            throw new ForbiddenException("Event ownership required");
        }
        if (role == AccountRole.STAFF) {
            if (eventStaffAssignmentRepository.existsByEventIdAndStaffUserIdAndActiveTrue(eventId, callerId)) {
                return;
            }
            throw new ForbiddenException("Staff user is not actively assigned to this event");
        }
        throw new ForbiddenException("Organizer or staff access required");
    }
}

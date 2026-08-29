package com.thedavelopers.eventqr.features.rewards.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.events.service.EventService;
import com.thedavelopers.eventqr.features.organizer.repository.EventStaffAssignmentRepository;
import com.thedavelopers.eventqr.features.rewards.model.dto.EventBenefitRequest;
import com.thedavelopers.eventqr.features.rewards.model.entity.EventBenefit;
import com.thedavelopers.eventqr.features.rewards.service.OrganizerBenefitService;
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/organizer/events/{eventId}/benefits")
public class OrganizerBenefitController {

    private final OrganizerBenefitService organizerBenefitService;
    private final JwtService jwtService;
    private final EventService eventService;
    private final EventStaffAssignmentRepository eventStaffAssignmentRepository;

    public OrganizerBenefitController(OrganizerBenefitService organizerBenefitService, JwtService jwtService,
                                      EventService eventService, EventStaffAssignmentRepository eventStaffAssignmentRepository) {
        this.organizerBenefitService = organizerBenefitService;
        this.jwtService = jwtService;
        this.eventService = eventService;
        this.eventStaffAssignmentRepository = eventStaffAssignmentRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventBenefit>>> list(HttpServletRequest request, @PathVariable UUID eventId) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(organizerBenefitService.list(eventId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventBenefit>> create(HttpServletRequest request,
                                                            @PathVariable UUID eventId,
                                                            @Valid @RequestBody EventBenefitRequest body) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success("Benefit created", organizerBenefitService.create(eventId, body)));
    }

    @PatchMapping("/{benefitId}")
    public ResponseEntity<ApiResponse<EventBenefit>> update(HttpServletRequest request,
                                                            @PathVariable UUID eventId,
                                                            @PathVariable UUID benefitId,
                                                            @Valid @RequestBody EventBenefitRequest body) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success("Benefit updated", organizerBenefitService.update(eventId, benefitId, body)));
    }

    @DeleteMapping("/{benefitId}")
    public ResponseEntity<ApiResponse<Void>> delete(HttpServletRequest request,
                                                    @PathVariable UUID eventId,
                                                    @PathVariable UUID benefitId) {
        requireOrganizerOrAdmin(request, eventId);
        organizerBenefitService.delete(eventId, benefitId);
        return ResponseEntity.ok(ApiResponse.success("Benefit deleted", null));
    }

    @GetMapping("/claims")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> claims(HttpServletRequest request,
                                                                         @PathVariable UUID eventId) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(organizerBenefitService.claims(eventId)));
    }

    @GetMapping("/benefit-claims")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> benefitClaims(HttpServletRequest request,
                                                                               @PathVariable UUID eventId) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(organizerBenefitService.claims(eventId)));
    }

    private void requireOrganizerOrAdmin(HttpServletRequest request, UUID eventId) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ATTENDEE) {
            throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Organizer or admin access required");
        }
        if (role == AccountRole.ORGANIZER) {
            if (eventService.findOne(eventId).organizerUserId().equals(callerId)) {
                return;
            }
            throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Event ownership required");
        }
        if (role == AccountRole.STAFF) {
            if (eventStaffAssignmentRepository.existsByEventIdAndStaffUserIdAndActiveTrue(eventId, callerId)) {
                return;
            }
            throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Staff user is not actively assigned to this event");
        }
        throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Organizer or admin access required");
    }
}
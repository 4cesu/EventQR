package com.thedavelopers.eventqr.features.events.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.events.model.dto.AttendeeEventResponse;
import com.thedavelopers.eventqr.features.events.model.dto.EventAvailabilityResponse;
import com.thedavelopers.eventqr.features.events.model.dto.EventApprovalRequest;
import com.thedavelopers.eventqr.features.events.model.dto.EventRequest;
import com.thedavelopers.eventqr.features.events.model.dto.EventResponse;
import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationRequest;
import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse;
import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationSubmissionResponse;
import com.thedavelopers.eventqr.features.registrations.service.RegistrationService;
import com.thedavelopers.eventqr.features.events.service.EventService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final JwtService jwtService;

    public EventController(EventService eventService, RegistrationService registrationService, JwtService jwtService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> create(HttpServletRequest request, @Valid @RequestBody EventRequest event) {
        requireAuthenticated(request);
        return ResponseEntity.ok(ApiResponse.success("Event submitted", eventService.create(event)));
    }

    @PutMapping("/{eventId}/review")
    public ResponseEntity<ApiResponse<EventResponse>> review(HttpServletRequest request,
                                                             @PathVariable UUID eventId,
                                                             @Valid @RequestBody EventApprovalRequest approvalRequest) {
        requireAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Event reviewed", eventService.review(eventId, approvalRequest)));
    }

    @PutMapping("/{eventId}/activate")
    public ResponseEntity<ApiResponse<EventResponse>> activate(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Event activated", eventService.activate(eventId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(eventService.findAllEvents()));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> findOne(@PathVariable UUID eventId) {
        return ResponseEntity.ok(ApiResponse.success(eventService.findOne(eventId)));
    }

    @GetMapping("/{eventId}/availability")
    public ResponseEntity<ApiResponse<EventAvailabilityResponse>> availability(@PathVariable UUID eventId) {
        return ResponseEntity.ok(ApiResponse.success(eventService.availability(eventId)));
    }

    @PostMapping("/{eventId}/registrations")
    public ResponseEntity<ApiResponse<RegistrationSubmissionResponse>> register(@PathVariable UUID eventId,
                                                                                @Valid @RequestBody RegistrationRequest request) {
        RegistrationRequest normalized = new RegistrationRequest(eventId, request.email(), request.fullName(), request.phoneNumber());
        return ResponseEntity.ok(ApiResponse.success("Registration completed", registrationService.register(normalized)));
    }

    @GetMapping("/attendee-visible")
    public ResponseEntity<ApiResponse<List<AttendeeEventResponse>>> listAttendeeVisible() {
        return ResponseEntity.ok(ApiResponse.success(eventService.findAttendeeVisibleEvents()));
    }

    private void requireAuthenticated(HttpServletRequest request) {
        jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
    }

    private void requireAdmin(HttpServletRequest request) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role != AccountRole.ADMIN && role != AccountRole.SUPER_ADMIN) {
            throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Admin access required");
        }
    }
}

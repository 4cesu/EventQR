package com.thedavelopers.eventqr.features.reports.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.events.service.EventService;
import com.thedavelopers.eventqr.features.organizer.model.dto.OrganizerDtos.OrganizerReportResponse;
import com.thedavelopers.eventqr.features.organizer.service.OrganizerService;
import com.thedavelopers.eventqr.features.reports.model.dto.EventReportSnapshot;
import com.thedavelopers.eventqr.features.reports.service.ReportService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/organizer/events/{eventId}/reports")
public class OrganizerReportController {

    private final ReportService reportService;
    private final OrganizerService organizerService;
    private final JwtService jwtService;
    private final EventService eventService;

    public OrganizerReportController(ReportService reportService, OrganizerService organizerService,
                                     JwtService jwtService, EventService eventService) {
        this.reportService = reportService;
        this.organizerService = organizerService;
        this.jwtService = jwtService;
        this.eventService = eventService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<OrganizerReportResponse>> summary(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(organizerService.report(currentUserId(request), eventId)));
    }


    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> attendance(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
    }

    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> entries(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
    }

    @GetMapping("/exits")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> exits(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
    }

    @GetMapping("/claims")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> claims(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
    }

    @GetMapping("/booth-visits")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> boothVisits(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
    }

    @GetMapping("/rewards")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> rewards(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
    }

    @GetMapping("/points")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> points(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
    }

    @PostMapping("/export")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> export(HttpServletRequest request, @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success("Export prepared", reportService.generate(eventId)));
    }

    private void requireAdminOrEventOwner(HttpServletRequest request, UUID eventId) {
        UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        if (role == AccountRole.ORGANIZER && eventService.findOne(eventId).organizerUserId().equals(callerId)) {
            return;
        }
        throw new ForbiddenException("Admin or event owner access required");
    }

    private UUID currentUserId(HttpServletRequest request) {
        return jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
    }
}
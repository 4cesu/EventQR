package com.thedavelopers.eventqr.features.reports.controller;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.events.service.EventService;
import com.thedavelopers.eventqr.features.reports.model.dto.EventReportSnapshot;
import com.thedavelopers.eventqr.features.reports.service.ReportService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final EventService eventService;
    private final JwtService jwtService;

    public ReportController(ReportService reportService, EventService eventService, JwtService jwtService) {
        this.reportService = reportService;
        this.eventService = eventService;
        this.jwtService = jwtService;
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<EventReportSnapshot>> generate(HttpServletRequest request,
                                                                     @PathVariable UUID eventId) {
        requireAdminOrEventOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
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
}
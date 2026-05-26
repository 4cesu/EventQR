package com.thedavelopers.eventqr.features.reports.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse;
import com.thedavelopers.eventqr.features.registrations.service.RegistrationService;
import com.thedavelopers.eventqr.features.reports.model.dto.EventReportSnapshot;
import com.thedavelopers.eventqr.features.reports.service.ReportService;
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse;
import com.thedavelopers.eventqr.features.transactions.service.TransactionService;
import com.thedavelopers.eventqr.shared.constants.RegistrationStatus;
import com.thedavelopers.eventqr.shared.constants.TransactionType;
import com.thedavelopers.eventqr.shared.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/organizer/events/{eventId}/attendance")
public class OrganizerAttendanceController {

    private final ReportService reportService;
    private final RegistrationService registrationService;
    private final TransactionService transactionService;

    public OrganizerAttendanceController(ReportService reportService, RegistrationService registrationService, TransactionService transactionService) {
        this.reportService = reportService;
        this.registrationService = registrationService;
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EventReportSnapshot>> summary(@PathVariable UUID eventId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generate(eventId)));
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> records(@PathVariable UUID eventId) {
        return ResponseEntity.ok(ApiResponse.success(registrationService.findByEvent(eventId)));
    }

    @GetMapping("/no-shows")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> noShows(@PathVariable UUID eventId) {
        List<RegistrationResponse> noShows = registrationService.findByEvent(eventId).stream()
                .filter(item -> item.status() == RegistrationStatus.NO_SHOW)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(noShows));
    }

    @GetMapping("/recent-checkins")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> recentCheckins(@PathVariable UUID eventId) {
        List<TransactionResponse> checkIns = transactionService.findByEvent(eventId).stream()
                .filter(transaction -> transaction.transactionType() == TransactionType.ENTRY
                        || transaction.transactionType() == TransactionType.ATTENDANCE)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(checkIns));
    }
}
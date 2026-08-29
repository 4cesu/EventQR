package com.thedavelopers.eventqr.features.idprinting.controller;

import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thedavelopers.eventqr.features.events.service.EventService;
import com.thedavelopers.eventqr.features.idprinting.model.dto.IdBatchPrintRequest;
import com.thedavelopers.eventqr.features.idprinting.model.dto.IdPrintResponse;
import com.thedavelopers.eventqr.features.idprinting.model.dto.IdTemplateRequest;
import com.thedavelopers.eventqr.features.idprinting.model.entity.IdTemplate;
import com.thedavelopers.eventqr.features.idprinting.service.IdPrintingService;
import com.thedavelopers.eventqr.features.organizer.repository.EventStaffAssignmentRepository;
import com.thedavelopers.eventqr.features.uploads.model.dto.StoredFileResponse;
import com.thedavelopers.eventqr.features.uploads.service.FileStorageService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1")
public class OrganizerIdTemplateController {

    private final IdPrintingService idPrintingService;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;
    private final EventService eventService;
    private final EventStaffAssignmentRepository eventStaffAssignmentRepository;

    public OrganizerIdTemplateController(IdPrintingService idPrintingService, JwtService jwtService,
                                         FileStorageService fileStorageService, EventService eventService,
                                         EventStaffAssignmentRepository eventStaffAssignmentRepository) {
        this.idPrintingService = idPrintingService;
        this.jwtService = jwtService;
        this.fileStorageService = fileStorageService;
        this.eventService = eventService;
        this.eventStaffAssignmentRepository = eventStaffAssignmentRepository;
    }

    @GetMapping("/organizer/events/{eventId}/id-templates")
    public ResponseEntity<ApiResponse<List<IdTemplate>>> list(HttpServletRequest request, @PathVariable UUID eventId) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(idPrintingService.listTemplates(eventId)));
    }

    @PostMapping("/organizer/events/{eventId}/id-template")
    public ResponseEntity<ApiResponse<IdTemplate>> create(HttpServletRequest request,
                                                          @PathVariable UUID eventId,
                                                          @Valid @RequestBody IdTemplateRequest body) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success("ID template saved", idPrintingService.saveTemplate(eventId, body)));
    }

    @GetMapping("/organizer/events/{eventId}/id-template")
    public ResponseEntity<ApiResponse<IdTemplate>> get(HttpServletRequest request, @PathVariable UUID eventId) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(idPrintingService.getTemplate(eventId)));
    }

    @PatchMapping("/organizer/events/{eventId}/id-template")
    public ResponseEntity<ApiResponse<IdTemplate>> update(HttpServletRequest request,
                                                          @PathVariable UUID eventId,
                                                          @Valid @RequestBody IdTemplateRequest body) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success("ID template updated", idPrintingService.saveTemplate(eventId, body)));
    }

    @GetMapping("/organizer/events/{eventId}/id-template/preview")
    public ResponseEntity<ApiResponse<IdTemplate>> preview(HttpServletRequest request, @PathVariable UUID eventId) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(idPrintingService.getTemplate(eventId)));
    }

    @PostMapping("/organizer/events/{eventId}/id-template/logo")
    public ResponseEntity<ApiResponse<StoredFileResponse>> uploadLogo(HttpServletRequest request,
                                                                       @PathVariable UUID eventId,
                                                                       @RequestParam("file") MultipartFile file) {
        requireOrganizerOrAdmin(request, eventId);
        return ResponseEntity.ok(ApiResponse.success("Logo stored", fileStorageService.store(eventId, "id-template-logo", file)));
    }

    @GetMapping("/staff/events/{eventId}/print-logs")
    public ResponseEntity<ApiResponse<List<IdPrintResponse>>> printLogs(HttpServletRequest request,
                                                                         @PathVariable UUID eventId) {
        requireStaffOrOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(idPrintingService.findByEvent(eventId)));
    }

    @PostMapping("/staff/events/{eventId}/attendees/{attendeeId}/id-preview")
    public ResponseEntity<ApiResponse<IdPrintResponse>> previewForAttendee(HttpServletRequest request,
                                                                            @PathVariable UUID eventId,
                                                                            @PathVariable UUID attendeeId) {
        requireStaffOrOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(idPrintingService.previewForAttendee(eventId, attendeeId)));
    }

    @PostMapping("/staff/events/{eventId}/attendees/{attendeeId}/print-id")
    public ResponseEntity<ApiResponse<IdPrintResponse>> print(HttpServletRequest request,
                                                              @PathVariable UUID eventId,
                                                              @PathVariable UUID attendeeId) {
        requireStaffOrOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(idPrintingService.printForAttendee(eventId, attendeeId, false)));
    }

    @PostMapping("/staff/events/{eventId}/attendees/{attendeeId}/reprint-id")
    public ResponseEntity<ApiResponse<IdPrintResponse>> reprint(HttpServletRequest request,
                                                                @PathVariable UUID eventId,
                                                                @PathVariable UUID attendeeId) {
        requireStaffOrOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(idPrintingService.printForAttendee(eventId, attendeeId, true)));
    }

    @PostMapping("/staff/events/{eventId}/print-id-batch")
    public ResponseEntity<ApiResponse<List<IdPrintResponse>>> printBatch(HttpServletRequest request,
                                                                         @PathVariable UUID eventId,
                                                                         @Valid @RequestBody IdBatchPrintRequest body) {
        requireStaffOrOwner(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(idPrintingService.printBatch(eventId, body.attendeeUserIds(), body.reprint())));
    }

    private void requireNonAttendee(HttpServletRequest request) {
        if (jwtService.extractRoleFromBearer(request.getHeader("Authorization")) == AccountRole.ATTENDEE) {
            throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Staff or organizer access required");
        }
    }

    private void requireStaffOrOwner(HttpServletRequest request, UUID eventId) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
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
        throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Staff or organizer access required");
    }

    private void requireOrganizerOrAdmin(HttpServletRequest request, UUID eventId) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        if (role == AccountRole.ORGANIZER) {
            UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
            if (eventService.findOne(eventId).organizerUserId().equals(callerId)) {
                return;
            }
            throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Event ownership required");
        }
        throw new com.thedavelopers.eventqr.shared.exceptions.ForbiddenException("Organizer or admin access required");
    }
}
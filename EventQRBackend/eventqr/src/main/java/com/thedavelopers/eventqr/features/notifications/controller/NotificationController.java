package com.thedavelopers.eventqr.features.notifications.controller;

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

import com.thedavelopers.eventqr.features.auditlogs.service.AuditLogService;
import com.thedavelopers.eventqr.features.events.service.EventService;
import com.thedavelopers.eventqr.features.notifications.model.dto.NotificationRequest;
import com.thedavelopers.eventqr.features.notifications.model.dto.NotificationResponse;
import com.thedavelopers.eventqr.features.notifications.service.NotificationService;
import com.thedavelopers.eventqr.features.organizer.repository.EventStaffAssignmentRepository;
import com.thedavelopers.eventqr.features.users.service.UserService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final UserService userService;
    private final EventService eventService;
    private final EventStaffAssignmentRepository eventStaffAssignmentRepository;

    public NotificationController(NotificationService notificationService, JwtService jwtService,
                                  AuditLogService auditLogService, UserService userService,
                                  EventService eventService, EventStaffAssignmentRepository eventStaffAssignmentRepository) {
        this.notificationService = notificationService;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
        this.userService = userService;
        this.eventService = eventService;
        this.eventStaffAssignmentRepository = eventStaffAssignmentRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> create(HttpServletRequest servletRequest,
                                                                    @Valid @RequestBody NotificationRequest request) {
        requireSenderRole(servletRequest);
        NotificationResponse response = notificationService.create(request);
        UUID userId = jwtService.extractUserIdFromBearer(servletRequest.getHeader("Authorization"));
        AccountRole role = jwtService.extractRoleFromBearer(servletRequest.getHeader("Authorization"));
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            auditLogService.log(
                    "NOTIFICATION_BROADCAST",
                    request.title(),
                    userId,
                    userService.findOne(userId).fullName(),
                    request.eventId(),
                    request.recipientUserId());
        }
        return ResponseEntity.ok(ApiResponse.success("Notification created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> mine(HttpServletRequest request) {
        UUID userId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        return ResponseEntity.ok(ApiResponse.success(notificationService.findByRecipient(userId)));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> findOne(HttpServletRequest request,
                                                                     @PathVariable UUID notificationId) {
        requireNotificationAccess(request, notificationId);
        return ResponseEntity.ok(ApiResponse.success(notificationService.findOne(notificationId)));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(HttpServletRequest request,
                                                                      @PathVariable UUID notificationId) {
        requireNotificationAccess(request, notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notificationService.markRead(notificationId)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(HttpServletRequest request) {
        UUID userId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> delete(HttpServletRequest request, @PathVariable UUID notificationId) {
        requireNotificationAccess(request, notificationId);
        notificationService.delete(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    @GetMapping("/recipient/{recipientUserId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> findByRecipient(HttpServletRequest request,
                                                                                   @PathVariable UUID recipientUserId) {
        requireRecipientOrAdmin(request, recipientUserId);
        return ResponseEntity.ok(ApiResponse.success(notificationService.findByRecipient(recipientUserId)));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> findByEvent(HttpServletRequest request,
                                                                              @PathVariable UUID eventId) {
        requireEventAccess(request, eventId);
        return ResponseEntity.ok(ApiResponse.success(notificationService.findByEvent(eventId)));
    }

    private void requireSenderRole(HttpServletRequest request) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ATTENDEE) {
            throw new ForbiddenException("Attendees cannot create notifications");
        }
    }

    private void requireNotificationAccess(HttpServletRequest request, UUID notificationId) {
        UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        NotificationResponse notification = notificationService.findOne(notificationId);
        if (notification.recipientUserId().equals(callerId)) {
            return;
        }
        throw new ForbiddenException("Access denied to notification");
    }

    private void requireRecipientOrAdmin(HttpServletRequest request, UUID recipientUserId) {
        UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        if (recipientUserId.equals(callerId)) {
            return;
        }
        throw new ForbiddenException("Access denied to notifications");
    }

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
        throw new ForbiddenException("Access denied to event notifications");
    }
}
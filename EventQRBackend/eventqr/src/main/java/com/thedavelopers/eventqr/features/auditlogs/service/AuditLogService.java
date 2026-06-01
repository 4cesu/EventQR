package com.thedavelopers.eventqr.features.auditlogs.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.thedavelopers.eventqr.features.auditlogs.model.dto.AuditLogRequest;
import com.thedavelopers.eventqr.features.auditlogs.model.dto.AuditLogResponse;
import com.thedavelopers.eventqr.features.auditlogs.model.entity.AuditLog;
import com.thedavelopers.eventqr.features.auditlogs.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public void log(AuditLogRequest request, UUID userId, String fullName) {
        log(request.action(), request.details(), userId, fullName, request.eventId(), request.targetUserId());
    }

    public void log(String action, String details, UUID userId, String fullName, UUID eventId, UUID targetUserId) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setDetails(details);
        log.setPerformedByUserId(userId);
        log.setPerformedByFullName(normalizeName(fullName));
        log.setEventId(eventId);
        log.setTargetUserId(targetUserId);
        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<AuditLogResponse> findByEvent(UUID eventId) {
        return auditLogRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getAction(), log.getDetails(), log.getPerformedByUserId(), log.getPerformedByFullName(), log.getEventId(), log.getTargetUserId(), log.getCreatedAt());
    }

    private String normalizeName(String fullName) {
        return fullName == null || fullName.isBlank() ? "Admin User" : fullName.trim();
    }
}

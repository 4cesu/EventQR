package com.thedavelopers.eventqr.features.qremail.controller;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thedavelopers.eventqr.features.qremail.service.QREmailService;
import com.thedavelopers.eventqr.features.registrations.service.RegistrationService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.interfaces.RegistrationLookupPort.RegistrationSnapshot;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1/qr-email")
public class QREmailController {

    private final QREmailService qrEmailService;
    private final RegistrationService registrationService;
    private final JwtService jwtService;

    public QREmailController(QREmailService qrEmailService, RegistrationService registrationService, JwtService jwtService) {
        this.qrEmailService = qrEmailService;
        this.registrationService = registrationService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registration/{registrationId}")
    public ResponseEntity<ApiResponse<QREmailService.DeliveryResult>> sendForRegistration(
            HttpServletRequest request, @PathVariable UUID registrationId) {
        requireAccess(request, registrationService.requireById(registrationId));
        return ResponseEntity.ok(ApiResponse.success("QR email delivery attempted",
                qrEmailService.sendForRegistration(registrationId)));
    }

    @PostMapping("/credential/{qrCredentialId}")
    public ResponseEntity<ApiResponse<QREmailService.DeliveryResult>> sendForCredential(
            HttpServletRequest request, @PathVariable UUID qrCredentialId) {
        RegistrationSnapshot registration = registrationService.findByQrCredentialId(qrCredentialId)
                .orElseThrow(() -> new com.thedavelopers.eventqr.shared.exceptions.ResourceNotFoundException(
                        "Registration not found for QR credential: " + qrCredentialId));
        requireAccess(request, registration);
        return ResponseEntity.ok(ApiResponse.success("QR email delivery attempted",
                qrEmailService.sendForCredential(qrCredentialId)));
    }

    private void requireAccess(HttpServletRequest request, RegistrationSnapshot registration) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ATTENDEE) {
            UUID userId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
            if (!registration.attendeeUserId().equals(userId)) {
                throw new ForbiddenException("You can only email your own QR credential");
            }
        }
    }
}

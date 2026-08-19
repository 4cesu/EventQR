package com.thedavelopers.eventqr.features.qremail.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thedavelopers.eventqr.features.qremail.model.EmailDeliveryStatus;
import com.thedavelopers.eventqr.features.qremail.model.entity.EmailDeliveryLog;
import com.thedavelopers.eventqr.features.qremail.repository.EmailDeliveryLogRepository;
import com.thedavelopers.eventqr.features.qrcredentials.model.entity.QrCredential;
import com.thedavelopers.eventqr.features.qrcredentials.repository.QrCredentialRepository;
import com.thedavelopers.eventqr.features.registrations.model.entity.EventRegistration;
import com.thedavelopers.eventqr.features.registrations.repository.EventRegistrationRepository;
import com.thedavelopers.eventqr.shared.exceptions.ResourceNotFoundException;
import com.thedavelopers.eventqr.shared.interfaces.QrCredentialPort;

@Service
public class QREmailService {

    private static final Logger log = LoggerFactory.getLogger(QREmailService.class);
    private static final long RETRY_DELAY_MILLIS = 250L;

    private final EventRegistrationRepository registrationRepository;
    private final QrCredentialRepository qrCredentialRepository;
    private final EmailDeliveryLogRepository deliveryLogRepository;
    private final QrCredentialPort qrCredentialPort;
    private final EmailTemplateBuilder templateBuilder;
    private final EmailGatewayService emailGatewayService;

    public QREmailService(EventRegistrationRepository registrationRepository,
                          QrCredentialRepository qrCredentialRepository,
                          EmailDeliveryLogRepository deliveryLogRepository,
                          QrCredentialPort qrCredentialPort,
                          EmailTemplateBuilder templateBuilder,
                          EmailGatewayService emailGatewayService) {
        this.registrationRepository = registrationRepository;
        this.qrCredentialRepository = qrCredentialRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.qrCredentialPort = qrCredentialPort;
        this.templateBuilder = templateBuilder;
        this.emailGatewayService = emailGatewayService;
    }

    @Transactional
    public DeliveryResult sendForRegistration(UUID registrationId) {
        EventRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + registrationId));
        QrCredential credential = qrCredentialRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("QR credential not found for registration: " + registrationId));
        return send(registration, credential);
    }

    @Transactional
    public DeliveryResult sendForCredential(UUID qrCredentialId) {
        QrCredential credential = qrCredentialRepository.findById(qrCredentialId)
                .orElseThrow(() -> new ResourceNotFoundException("QR credential not found: " + qrCredentialId));
        EventRegistration registration = registrationRepository.findByQrCredentialId(qrCredentialId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found for QR credential: " + qrCredentialId));
        return send(registration, credential);
    }

    public DeliveryResult sendForRegistrationSafely(UUID registrationId) {
        try {
            log.info("QR email delivery requested registrationId={}", registrationId);
            return sendForRegistration(registrationId);
        } catch (Exception exception) {
            log.error("QR email delivery aborted registrationId={} reason={}", registrationId,
                    exception.getMessage(), exception);
            return new DeliveryResult(registrationId, null, EmailDeliveryStatus.FAILED);
        }
    }

    private DeliveryResult send(EventRegistration registration, QrCredential credential) {
        String recipientEmail = registration.getAttendeeEmail();
        log.info("Preparing QR email registrationId={} qrCredentialId={} recipient={}",
            registration.getId(), credential.getId(), recipientEmail);
        EmailDeliveryLog deliveryLog = newLog(registration, credential, recipientEmail, EmailDeliveryStatus.RETRY_PENDING, null);
        saveLog(deliveryLog);
        if (!isValidEmail(recipientEmail)) {
            return fail(registration, credential, deliveryLog, "Attendee email is missing or invalid");
        }

        try {
            sendMessage(recipientEmail, registration.getAttendeeName(), credential.getQrValue());
            return sent(registration, credential, deliveryLog);
        } catch (Exception firstFailure) {
            log.warn("QR email first attempt failed registrationId={} reason={}",
                    registration.getId(), firstFailure.getMessage());
            updateLog(deliveryLog, EmailDeliveryStatus.RETRY_PENDING, firstFailure.getMessage());
            sleepBeforeRetry();
            try {
                sendMessage(recipientEmail, registration.getAttendeeName(), credential.getQrValue());
                return sent(registration, credential, deliveryLog);
            } catch (Exception secondFailure) {
                String error = secondFailure.getMessage() == null ? firstFailure.getMessage() : secondFailure.getMessage();
                log.error("QR email retry failed registrationId={} reason={}", registration.getId(), error, secondFailure);
                return fail(registration, credential, deliveryLog, error);
            }
        }
    }

    private void sendMessage(String recipientEmail, String attendeeName, String qrValue) {
        byte[] qrImageBytes = qrCredentialPort.renderQrImage(qrValue);
        log.debug("QR image bytes retrieved registrationEmail={} byteCount={}", recipientEmail, qrImageBytes.length);
        emailGatewayService.send(recipientEmail, templateBuilder.build(attendeeName, qrValue, qrImageBytes));
    }

    private DeliveryResult sent(EventRegistration registration, QrCredential credential, EmailDeliveryLog deliveryLog) {
        updateLog(deliveryLog, EmailDeliveryStatus.SENT, null);
        qrCredentialPort.markEmailSent(credential.getId());
        log.info("QR email sent registrationId={} qrCredentialId={}", registration.getId(), credential.getId());
        return new DeliveryResult(registration.getId(), credential.getId(), EmailDeliveryStatus.SENT);
    }

    private DeliveryResult fail(EventRegistration registration, QrCredential credential, EmailDeliveryLog deliveryLog, String error) {
        updateLog(deliveryLog, EmailDeliveryStatus.FAILED, error);
        qrCredentialPort.markEmailFailed(credential.getId());
        log.warn("QR email delivery failed for registration {}: {}", registration.getId(), error);
        return new DeliveryResult(registration.getId(), credential.getId(), EmailDeliveryStatus.FAILED);
    }

    private EmailDeliveryLog newLog(EventRegistration registration, QrCredential credential, String recipientEmail,
                                    EmailDeliveryStatus status, String error) {
        EmailDeliveryLog deliveryLog = new EmailDeliveryLog();
        deliveryLog.setRecipientEmail(recipientEmail == null ? "" : recipientEmail);
        deliveryLog.setRegistrationId(registration.getId());
        deliveryLog.setQrCredentialId(credential.getId());
        deliveryLog.setStatus(status);
        deliveryLog.setAttemptedAt(Instant.now());
        deliveryLog.setErrorMessage(error);
        return deliveryLog;
    }

    private void updateLog(EmailDeliveryLog deliveryLog, EmailDeliveryStatus status, String error) {
        deliveryLog.setStatus(status);
        deliveryLog.setAttemptedAt(Instant.now());
        deliveryLog.setErrorMessage(error);
        saveLog(deliveryLog);
    }

    private void saveLog(EmailDeliveryLog deliveryLog) {
        try {
            deliveryLogRepository.save(deliveryLog);
        } catch (RuntimeException exception) {
            log.warn("Could not persist QR email delivery log: {}", exception.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    public record DeliveryResult(UUID registrationId, UUID qrCredentialId, EmailDeliveryStatus status) {
    }
}

package com.thedavelopers.eventqr.features.qremail.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.thedavelopers.eventqr.shared.interfaces.RegistrationEmailRequestedEvent;

/**
 * Listens for the after-commit registration event and dispatches the QR email to the
 * bounded async executor. The listener itself runs on the publishing (request) thread
 * briefly — it only submits to the executor, so Tomcat threads are never blocked by
 * the email gateway calls or the retry sleep inside {@link QREmailService}.
 */
@Component
public class RegistrationEmailListener {

    private final QREmailService qrEmailService;

    public RegistrationEmailListener(QREmailService qrEmailService) {
        this.qrEmailService = qrEmailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRegistrationEmailRequested(RegistrationEmailRequestedEvent event) {
        qrEmailService.sendForRegistrationSafelyAsync(event.registrationId());
    }
}
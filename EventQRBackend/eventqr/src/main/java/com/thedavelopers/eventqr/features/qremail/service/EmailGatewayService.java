package com.thedavelopers.eventqr.features.qremail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

@Service
public class EmailGatewayService {

    private static final Logger log = LoggerFactory.getLogger(EmailGatewayService.class);

    private final Resend resend;

    public EmailGatewayService(Resend resend) {
        this.resend = resend;
    }

    public void send(String recipientEmail, EmailTemplateBuilder.EmailContent content) {
        try {
            log.debug("Sending QR email through Resend REST API recipient={}", recipientEmail);
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from("EventQR <onboarding@resend.dev>")
                    .to(recipientEmail)
                    .subject(content.subject())
                    .html(content.html())
                    .build();
            resend.emails().send(options);
            log.debug("Resend REST API accepted QR email recipient={}", recipientEmail);
        } catch (ResendException | RuntimeException exception) {
            throw new IllegalStateException("QR email could not be sent through Resend REST API", exception);
        }
    }
}

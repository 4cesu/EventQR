package com.thedavelopers.eventqr.features.qremail.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailGatewayService {

    private static final Logger log = LoggerFactory.getLogger(EmailGatewayService.class);

    private final JavaMailSender mailSender;

    public EmailGatewayService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(MimeMessage message) {
        try {
            log.debug("Sending QR email through configured SMTP gateway");
            mailSender.send(message);
            log.debug("SMTP gateway accepted QR email");
        } catch (RuntimeException exception) {
            throw new IllegalStateException("QR email could not be sent through Resend SMTP", exception);
        }
    }
}

package com.thedavelopers.eventqr.features.qremail.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailGatewayService {

    private final JavaMailSender mailSender;

    public EmailGatewayService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(MimeMessage message) {
        try {
            mailSender.send(message);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("QR email could not be sent through Gmail SMTP", exception);
        }
    }
}

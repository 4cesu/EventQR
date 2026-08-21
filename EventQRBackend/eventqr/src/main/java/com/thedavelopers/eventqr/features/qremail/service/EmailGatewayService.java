package com.thedavelopers.eventqr.features.qremail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import brevo.ApiException;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailAttachment;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;

@Service
public class EmailGatewayService {

    private static final Logger log = LoggerFactory.getLogger(EmailGatewayService.class);

    private final TransactionalEmailsApi transactionalEmailsApi;
    private final String senderEmail;

    public EmailGatewayService(TransactionalEmailsApi transactionalEmailsApi,
                               @org.springframework.beans.factory.annotation.Value("${brevo.sender.email}") String senderEmail) {
        this.transactionalEmailsApi = transactionalEmailsApi;
        this.senderEmail = senderEmail;
    }

    public void send(String recipientEmail, EmailTemplateBuilder.EmailContent content) {
        try {
            String contentId = "qr-code";
            SendSmtpEmailAttachment qrAttachment = new SendSmtpEmailAttachment()
                    .content(content.qrImageBytes())
                    .name("qrcode.png")
                    .inline(true)
                    .contentId(contentId);
            log.debug("Sending QR code as inline attachment recipient={} contentId={} byteCount={}",
                recipientEmail, contentId, content.qrImageBytes().length);
            log.debug("Sending QR email through Brevo REST API recipient={}", recipientEmail);
            SendSmtpEmail email = new SendSmtpEmail()
                    .sender(new SendSmtpEmailSender().email(senderEmail).name("EventQR"))
                    .addToItem(new SendSmtpEmailTo().email(recipientEmail))
                    .subject(content.subject())
                    .htmlContent(content.html())
                    .addAttachmentItem(qrAttachment);
            transactionalEmailsApi.sendTransacEmail(email);
            log.debug("Brevo REST API accepted QR email recipient={}", recipientEmail);
        } catch (ApiException | RuntimeException exception) {
            throw new IllegalStateException("QR email could not be sent through Brevo REST API", exception);
        }
    }
}

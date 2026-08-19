package com.thedavelopers.eventqr.features.qremail.service;

import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class EmailTemplateBuilder {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateBuilder.class);

    public EmailContent build(String attendeeName, String qrValue, byte[] qrImageBytes) {
        if (qrImageBytes == null || qrImageBytes.length == 0) {
            throw new IllegalArgumentException("QR image bytes must not be empty");
        }
        log.debug("Building QR email image rawBytes={}", qrImageBytes.length);
        String base64Image = Base64.getEncoder().encodeToString(qrImageBytes);
        log.debug("Encoded QR email image base64Length={}", base64Image.length());
        return new EmailContent("Your EventQR credential", buildHtml(attendeeName, qrValue, base64Image));
    }

    private String buildHtml(String attendeeName, String qrValue, String base64Image) {
        String safeName = HtmlUtils.htmlEscape(attendeeName == null ? "Attendee" : attendeeName);
        return """
                <!doctype html>
                <html><body>
                <p>Hello %s,</p>
                <p>Here is your EventQR credential. Present this QR code at the event when requested.</p>
                <p><img src="data:image/png;base64,%s" alt="EventQR credential" style="max-width: 250px; height: auto;" /></p>
                <p>Your credential value: <strong>%s</strong></p>
                <p>Keep this email available as your backup QR access.</p>
                </body></html>
                """.formatted(safeName, base64Image, HtmlUtils.htmlEscape(qrValue));
    }

    public record EmailContent(String subject, String html) {
    }
}

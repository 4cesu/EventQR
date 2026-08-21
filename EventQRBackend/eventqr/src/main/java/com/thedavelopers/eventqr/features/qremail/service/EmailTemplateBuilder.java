package com.thedavelopers.eventqr.features.qremail.service;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class EmailTemplateBuilder {

    public EmailContent build(String attendeeName, String qrValue, byte[] qrImageBytes) {
        if (qrImageBytes == null || qrImageBytes.length == 0) {
            throw new IllegalArgumentException("QR image bytes must not be empty");
        }
        return new EmailContent("Your EventQR credential", buildHtml(attendeeName, qrValue), qrImageBytes);
    }

    private String buildHtml(String attendeeName, String qrValue) {
        String safeName = HtmlUtils.htmlEscape(attendeeName == null ? "Attendee" : attendeeName);
        return """
                <!doctype html>
                <html><body>
                <p>Hello %s,</p>
                <p>Here is your EventQR credential. Present this QR code at the event when requested.</p>
                <p><img src="cid:qrImage.png" alt="EventQR credential" style="max-width: 250px; height: auto;" /></p>
                <p>Your credential value: <strong>%s</strong></p>
                <p>Keep this email available as your backup QR access.</p>
                </body></html>
                """.formatted(safeName, HtmlUtils.htmlEscape(qrValue));
    }

    public record EmailContent(String subject, String html, byte[] qrImageBytes) {
    }
}

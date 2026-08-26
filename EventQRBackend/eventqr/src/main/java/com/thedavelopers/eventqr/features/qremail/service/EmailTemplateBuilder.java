package com.thedavelopers.eventqr.features.qremail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class EmailTemplateBuilder {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplateBuilder.class);

    public EmailContent build(String attendeeName, String qrValue, byte[] qrImageBytes, String attendeeId) {
        if (qrImageBytes == null || qrImageBytes.length == 0) {
            throw new IllegalArgumentException("QR image bytes must not be empty");
        }
        log.debug("Building QR email image rawBytes={}", qrImageBytes.length);
        return new EmailContent("Your EventQR credential", buildHtml(attendeeName, qrValue, attendeeId), qrImageBytes);
    }

    private String buildHtml(String attendeeName, String qrValue, String attendeeId) {
        String safeName = HtmlUtils.htmlEscape(attendeeName == null ? "Attendee" : attendeeName);
        String safeId = attendeeId != null ? HtmlUtils.htmlEscape(attendeeId) : null;
        String idLine = safeId != null
                ? "<p>Your Attendee ID: <strong>%s</strong> — show this or your QR at the event.</p>".formatted(safeId)
                : "";
        return """
                <!doctype html>
                <html><body>
                <p>Hello %s,</p>
                %s
                <p>Here is your EventQR credential. Present this QR code at the event when requested.</p>
                <p><img src="cid:qrImage.png" alt="EventQR credential" style="max-width: 250px;" /></p>
                </body></html>
                """.formatted(safeName, idLine);
    }

    public record EmailContent(String subject, String html, byte[] qrImageBytes) {
    }
}

package com.thedavelopers.eventqr.features.qremail.service;

import java.util.Base64;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class EmailTemplateBuilder {

    public EmailContent build(String attendeeName, String qrValue, byte[] qrImageBytes) {
        String base64Image = Base64.getEncoder().encodeToString(qrImageBytes);
        return new EmailContent("Your EventQR credential", buildHtml(attendeeName, qrValue, base64Image));
    }

    private String buildHtml(String attendeeName, String qrValue, String base64Image) {
        String safeName = HtmlUtils.htmlEscape(attendeeName == null ? "Attendee" : attendeeName);
        return """
                <!doctype html>
                <html><body>
                <p>Hello %s,</p>
                <p>Here is your EventQR credential. Present this QR code at the event when requested.</p>
                <p><img src="data:image/png;base64,%s" alt="EventQR credential" style="max-width: 200px;" /></p>
                <p>Your credential value: <strong>%s</strong></p>
                <p>Keep this email available as your backup QR access.</p>
                </body></html>
                """.formatted(safeName, base64Image, HtmlUtils.htmlEscape(qrValue));
    }

    public record EmailContent(String subject, String html) {
    }
}

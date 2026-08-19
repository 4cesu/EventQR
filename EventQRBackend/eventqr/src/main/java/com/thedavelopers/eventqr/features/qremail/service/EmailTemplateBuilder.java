package com.thedavelopers.eventqr.features.qremail.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class EmailTemplateBuilder {

    public EmailContent build(String attendeeName, String qrValue) {
        byte[] qrImage = renderQrCode(qrValue);
        return new EmailContent("Your EventQR credential", buildHtml(attendeeName, qrValue, qrImage));
    }

    private String buildHtml(String attendeeName, String qrValue, byte[] qrImage) {
        String safeName = HtmlUtils.htmlEscape(attendeeName == null ? "Attendee" : attendeeName);
        String qrImageData = Base64.getEncoder().encodeToString(qrImage);
        return """
                <!doctype html>
                <html><body>
                <p>Hello %s,</p>
                <p>Here is your EventQR credential. Present this QR code at the event when requested.</p>
                <p><img src="data:image/png;base64,%s" alt="EventQR credential" width="320" height="320"></p>
                <p>Your credential value: <strong>%s</strong></p>
                <p>Keep this email available as your backup QR access.</p>
                </body></html>
                """.formatted(safeName, qrImageData, HtmlUtils.htmlEscape(qrValue));
    }

    private byte[] renderQrCode(String qrValue) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(qrValue, BarcodeFormat.QR_CODE, 320, 320,
                    Map.of(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name()));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("QR image could not be generated", exception);
        }
    }

    public record EmailContent(String subject, String html) {
    }
}

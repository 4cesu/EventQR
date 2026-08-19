package com.thedavelopers.eventqr.features.qremail.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Component
public class EmailTemplateBuilder {

    private static final String CONTENT_ID = "eventqr-credential";

    public void populate(MimeMessage message, String recipientEmail, String attendeeName, String qrValue) {
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(recipientEmail);
            helper.setSubject("Your EventQR credential");
            helper.setText(buildHtml(attendeeName, qrValue), true);
            helper.addInline(CONTENT_ID, new ByteArrayResource(renderQrCode(qrValue)), "image/png");
        } catch (MessagingException exception) {
            throw new IllegalStateException("QR email content could not be built", exception);
        }
    }

    private String buildHtml(String attendeeName, String qrValue) {
        String safeName = HtmlUtils.htmlEscape(attendeeName == null ? "Attendee" : attendeeName);
        return """
                <!doctype html>
                <html><body>
                <p>Hello %s,</p>
                <p>Here is your EventQR credential. Present this QR code at the event when requested.</p>
                <p><img src="cid:%s" alt="EventQR credential" width="320" height="320"></p>
                <p>Your credential value: <strong>%s</strong></p>
                <p>Keep this email available as your backup QR access.</p>
                </body></html>
                """.formatted(safeName, CONTENT_ID, HtmlUtils.htmlEscape(qrValue));
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
}

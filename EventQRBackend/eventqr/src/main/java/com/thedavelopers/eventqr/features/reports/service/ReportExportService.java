package com.thedavelopers.eventqr.features.reports.service;

import static com.thedavelopers.eventqr.features.reports.model.dto.EventReportDtos.EventReportResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import com.thedavelopers.eventqr.features.reports.model.ReportExportFormat;
import com.thedavelopers.eventqr.shared.exceptions.BadRequestException;

@Service
public class ReportExportService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    public ExportPayload export(EventReportResponse report, ReportExportFormat format) {
        if (format == ReportExportFormat.PDF) {
            return toPdf(report);
        }
        return toCsv(report);
    }

    private ExportPayload toCsv(EventReportResponse report) {
        StringBuilder builder = new StringBuilder();
        builder.append(csv(report.reportTitle())).append('\n');
        builder.append(csv("Event")).append(',').append(csv(report.eventName())).append('\n');
        builder.append(csv("Generated")).append(',').append(csv(STAMP.format(report.generatedAt()))).append("\n\n");

        List<String> columns = report.columns();
        builder.append(String.join(",", columns.stream().map(this::csv).toList())).append('\n');
        for (var row : report.rows()) {
            List<String> values = new ArrayList<>(row.values());
            while (values.size() < columns.size()) {
                values.add("");
            }
            builder.append(String.join(",", values.stream().map(this::csv).toList())).append('\n');
        }

        builder.append("\n").append(csv("Chart Summary")).append('\n');
        for (Map.Entry<String, Long> entry : report.chartSeries().entrySet()) {
            builder.append(csv(entry.getKey())).append(',').append(csv(String.valueOf(entry.getValue()))).append('\n');
        }

        String fileName = slug(report.reportType().name().toLowerCase(Locale.ENGLISH)) + "-"
                + report.eventId() + ".csv";
        return new ExportPayload(builder.toString().getBytes(StandardCharsets.UTF_8), "text/csv", fileName);
    }

    private ExportPayload toPdf(EventReportResponse report) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - 56;
                y = writeLine(stream, 16, true, 56, y, report.reportTitle());
                y = writeLine(stream, 11, false, 56, y - 4, "Event: " + report.eventName());
                y = writeLine(stream, 10, false, 56, y - 2, "Generated: " + STAMP.format(report.generatedAt()));

                y -= 16;
                y = writeLine(stream, 10, true, 56, y, String.join(" | ", report.columns()));
                y -= 4;

                for (var row : report.rows()) {
                    String line = String.join(" | ", row.values());
                    y = writeLine(stream, 9, false, 56, y, truncate(line, 120));
                    if (y <= 72) {
                        break;
                    }
                }

                y -= 12;
                y = writeLine(stream, 10, true, 56, y, "Chart Summary");
                for (Map.Entry<String, Long> entry : report.chartSeries().entrySet()) {
                    y = writeLine(stream, 9, false, 56, y, entry.getKey() + ": " + entry.getValue());
                    if (y <= 72) {
                        break;
                    }
                }
            }

            document.save(output);
            String fileName = slug(report.reportType().name().toLowerCase(Locale.ENGLISH)) + "-"
                    + report.eventId() + ".pdf";
            return new ExportPayload(output.toByteArray(), "application/pdf", fileName);
        } catch (IOException ex) {
            throw new BadRequestException("Unable to export PDF report");
        }
    }

    private float writeLine(PDPageContentStream stream,
                            int size,
                            boolean bold,
                            float x,
                            float y,
                            String text) throws IOException {
        stream.beginText();
        stream.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, size);
        stream.newLineAtOffset(x, y);
        stream.showText(text == null ? "" : text);
        stream.endText();
        return y - (size + 4);
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        String escaped = safe.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String slug(String value) {
        return value.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    public record ExportPayload(byte[] bytes, String contentType, String fileName) {
    }
}

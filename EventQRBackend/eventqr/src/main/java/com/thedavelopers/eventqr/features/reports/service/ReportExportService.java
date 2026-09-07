package com.thedavelopers.eventqr.features.reports.service;

import static com.thedavelopers.eventqr.features.reports.model.dto.EventReportDtos.EventReportResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
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

@Service
public class ReportExportService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            .withZone(ZoneId.of("Asia/Manila"));

    private static final float MARGIN = 56f;
    private static final float BOTTOM_MARGIN = 72f;

    public String contentType(ReportExportFormat format) {
        return format == ReportExportFormat.PDF ? "application/pdf" : "text/csv";
    }

    public String fileName(EventReportResponse report, ReportExportFormat format) {
        String ext = format == ReportExportFormat.PDF ? "pdf" : "csv";
        return slug(report.reportType().name().toLowerCase(Locale.ENGLISH)) + "-" + report.eventId() + "." + ext;
    }

    public void writeCsv(EventReportResponse report, OutputStream output) {
        try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            writeLine(writer, csv(report.reportTitle()));
            writeLine(writer, csv("Event") + "," + csv(report.eventName()));
            writeLine(writer, csv("Generated") + "," + csv(STAMP.format(report.generatedAt())));
            writeLine(writer, "");

            List<String> columns = report.columns();
            writeLine(writer, String.join(",", columns.stream().map(this::csv).toList()));

            for (var row : report.rows()) {
                List<String> values = new ArrayList<>(row.values());
                while (values.size() < columns.size()) {
                    values.add("");
                }
                writeLine(writer, String.join(",", values.stream().map(this::csv).toList()));
            }

            writeLine(writer, "");
            writeLine(writer, csv("Chart Summary"));
            for (Map.Entry<String, Long> entry : report.chartSeries().entrySet()) {
                writeLine(writer, csv(entry.getKey()) + "," + csv(String.valueOf(entry.getValue())));
            }
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to stream CSV report", ex);
        }
    }

    public void writePdf(EventReportResponse report, OutputStream output) {
        try (PDDocument document = new PDDocument()) {
            writePdfContent(document, report);
            document.save(output);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to stream PDF report", ex);
        }
    }

    private void writePdfContent(PDDocument document, EventReportResponse report) throws IOException {
        PageCursor cursor = newCursor(document);

        writeHeader(cursor, report);

        float tableWidth = cursor.page.getMediaBox().getWidth() - 2 * MARGIN;
        List<String> columns = report.columns();
        int colCount = Math.max(columns.size(), 1);
        float[] colWidths = new float[colCount];
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = tableWidth / colCount;
        }

        writeRow(cursor, columns.toArray(new String[0]), colWidths, true, 10);
        cursor.y -= 4;

        for (var row : report.rows()) {
            List<String> values = new ArrayList<>(row.values());
            while (values.size() < colCount) {
                values.add("");
            }
            String[] cells = values.toArray(new String[0]);
            writeRow(cursor, cells, colWidths, false, 9);
        }

        cursor.y -= 12;
        cursor = ensureSpace(cursor, 16);
        cursor.y = writeLine(cursor, 10, true, MARGIN, cursor.y, "Chart Summary");
        for (Map.Entry<String, Long> entry : report.chartSeries().entrySet()) {
            cursor = ensureSpace(cursor, 14);
            cursor.y = writeLine(cursor, 9, false, MARGIN, cursor.y, entry.getKey() + ": " + entry.getValue());
        }

        cursor.stream.close();
    }

    private PageCursor newCursor(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);
        float startY = page.getMediaBox().getHeight() - MARGIN;
        return new PageCursor(document, page, stream, startY);
    }

    private PageCursor ensureSpace(PageCursor cursor, float needed) throws IOException {
        if (cursor.y - needed >= BOTTOM_MARGIN) {
            return cursor;
        }
        cursor.stream.close();
        return newCursor(cursor.document);
    }

    private void writeHeader(PageCursor cursor, EventReportResponse report) throws IOException {
        cursor.y = writeLine(cursor, 16, true, MARGIN, cursor.y, report.reportTitle());
        cursor.y = writeLine(cursor, 11, false, MARGIN, cursor.y - 4, "Event: " + report.eventName());
        cursor.y = writeLine(cursor, 10, false, MARGIN, cursor.y - 2, "Generated: " + STAMP.format(report.generatedAt()));
        cursor.y -= 16;
    }

    private void writeRow(PageCursor cursor, String[] cells, float[] widths, boolean bold, int size) throws IOException {
        cursor = ensureSpace(cursor, size + 6);
        float x = MARGIN;
        for (int i = 0; i < cells.length; i++) {
            float w = i < widths.length ? widths[i] : 0;
            String text = truncate(cells[i] == null ? "" : cells[i], Math.max(8, (int) (w / (size * 0.55f))));
            writeLine(cursor, size, bold, x, cursor.y, text);
            x += w;
        }
        cursor.y -= (size + 4);
    }

    private float writeLine(PageCursor cursor,
                            int size,
                            boolean bold,
                            float x,
                            float y,
                            String text) throws IOException {
        cursor.stream.beginText();
        cursor.stream.setFont(bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, size);
        cursor.stream.newLineAtOffset(x, y);
        cursor.stream.showText(text == null ? "" : text);
        cursor.stream.endText();
        return y - (size + 4);
    }

    private void writeLine(Writer writer, String value) throws IOException {
        writer.write(value);
        writer.write("\r\n");
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
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String slug(String value) {
        return value.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private static final class PageCursor {
        final PDDocument document;
        PDPage page;
        PDPageContentStream stream;
        float y;

        PageCursor(PDDocument document, PDPage page, PDPageContentStream stream, float y) {
            this.document = document;
            this.page = page;
            this.stream = stream;
            this.y = y;
        }
    }
}

package com.thedavelopers.eventqr.shared.response;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {

    /**
     * Serializes this record to a compact JSON object string, mirroring the shape that
     * Spring's Jackson mapper produces for the same record so that error responses are
     * consistent whether they originate from {@code GlobalExceptionHandler} or from
     * servlet-level filters (e.g. {@code RateLimitFilter}) that bypass the exception
     * handling pipeline.
     */
    public String toJson() {
        return "{\"timestamp\":\"" + timestamp + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + escape(error) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"path\":\"" + escape(path) + "\"}";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
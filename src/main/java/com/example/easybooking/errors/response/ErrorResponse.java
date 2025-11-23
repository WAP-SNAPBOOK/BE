package com.example.easybooking.errors.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String code;
    private final String message;
    private final String path;
    private final String traceId;
    private final Instant timestamp;
    private final List<String> details;

    /**
     * Create an ErrorResponse populated with the provided error attributes.
     *
     * @param code      application-specific error code
     * @param message   human-readable error message
     * @param path      request path or endpoint where the error occurred (may be null)
     * @param traceId   correlation or tracing identifier for the request (may be null)
     * @param timestamp instant when the error occurred (may be null)
     * @param details   optional list of detailed error messages or validation problems (may be null)
     * @return          a new ErrorResponse instance containing the supplied values
     */
    public static ErrorResponse of(String code,
                                   String message,
                                   String path,
                                   String traceId,
                                   Instant timestamp,
                                   List<String> details) {
        return new ErrorResponse(code, message, path, traceId, timestamp, details);
    }
}
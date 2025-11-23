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

    public static ErrorResponse of(String code,
                                   String message,
                                   String path,
                                   String traceId,
                                   Instant timestamp,
                                   List<String> details) {
        return new ErrorResponse(code, message, path, traceId, timestamp, details);
    }
}

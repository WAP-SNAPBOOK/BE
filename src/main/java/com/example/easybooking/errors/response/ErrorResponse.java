package com.example.easybooking.errors.response;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
public class ErrorResponse {
    private final String code;
    private final String message;
    private final String path;
    private final String traceId;
    private final Instant timestamp;
    private final List<String> errors;
}

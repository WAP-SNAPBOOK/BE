package com.example.easybooking.errors.handler;

import com.example.easybooking.common.filter.TraceIdFilter;
import com.example.easybooking.errors.errorcode.CommonErrorCode;
import com.example.easybooking.errors.errorcode.ErrorCode;
import com.example.easybooking.errors.exception.BaseBusinessException;
import com.example.easybooking.errors.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle a BaseBusinessException thrown during request processing and produce an HTTP error response.
     *
     * Uses the exception's ErrorCode and detail message to determine the HTTP status and response body.
     *
     * @param e       the business exception containing an ErrorCode and optional detail message
     * @param request the HTTP request that triggered the exception
     * @return        a ResponseEntity containing an ErrorResponse constructed from the exception's error code and message
     */
    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BaseBusinessException e,
                                                                 HttpServletRequest request) {
        log.error("[{}][{} {}] {}", currentTraceId(), request.getMethod(), request.getRequestURI(),
                e.getErrorCode().name(), e);
        return buildResponse(e.getErrorCode(), e.getDetailMessage(), request);
    }

    /**
     * Handles any uncaught exception by producing a standardized internal-server-error response.
     *
     * @param e       the uncaught exception
     * @param request the current HTTP request used to populate response metadata
     * @return a ResponseEntity containing an ErrorResponse populated with the internal server error code and request metadata
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e,
                                                                   HttpServletRequest request) {
        log.error("[{}][{} {}] Unexpected error", currentTraceId(), request.getMethod(),
                request.getRequestURI(), e);
        return buildResponse(CommonErrorCode.INTERNAL_SERVER_ERROR, null, request);
    }

    /**
     * Builds a ResponseEntity carrying an ErrorResponse populated from the provided ErrorCode.
     *
     * @param errorCode     the error code that determines the HTTP status and default message
     * @param customMessage an optional message that overrides the error code's default message when non-null
     * @param request       the HTTP request whose metadata (such as URI) will be included in the error response; may be null
     * @return              a ResponseEntity with the HTTP status from {@code errorCode} and an {@code ErrorResponse} body
     */
    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode,
                                                        String customMessage,
                                                        HttpServletRequest request) {
        String message = customMessage != null ? customMessage : errorCode.getMessage();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(makeErrorResponse(errorCode, message, request, null));
    }

    /**
     * Builds an ErrorResponse containing the error code, message, trace and request context.
     *
     * @param errorCode the error code whose name will be set on the response
     * @param message   the human-readable message to include; may be null
     * @param request   the HTTP request from which the request URI will be extracted; may be null
     * @param details   optional additional detail strings to include; may be null
     * @return an ErrorResponse with the error code name, message, request URI (if available), current trace id, timestamp, and provided details
     */
    private ErrorResponse makeErrorResponse(ErrorCode errorCode,
                                            String message,
                                            HttpServletRequest request,
                                            List<String> details) {
        return ErrorResponse.of(
                errorCode.name(),
                message,
                request != null ? request.getRequestURI() : null,
                currentTraceId(),
                Instant.now(),
                details
        );
    }

    /**
     * Retrieve the current request trace identifier from the MDC.
     *
     * @return the current trace identifier from MDC, or {@code no-trace} if none is present
     */
    private String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return traceId != null ? traceId : "no-trace";
    }
}
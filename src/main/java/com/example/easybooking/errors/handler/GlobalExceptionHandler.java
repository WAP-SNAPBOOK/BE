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

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BaseBusinessException e,
                                                                 HttpServletRequest request) {
        log.error("[{}][{} {}] {}", currentTraceId(), request.getMethod(), request.getRequestURI(),
                e.getErrorCode().name(), e);
        return buildResponse(e.getErrorCode(), e.getDetailMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e,
                                                                   HttpServletRequest request) {
        log.error("[{}][{} {}] Unexpected error", currentTraceId(), request.getMethod(),
                request.getRequestURI(), e);
        return buildResponse(CommonErrorCode.INTERNAL_SERVER_ERROR, null, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode,
                                                        String customMessage,
                                                        HttpServletRequest request) {
        String message = customMessage != null ? customMessage : errorCode.getMessage();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(makeErrorResponse(errorCode, message, request, null));
    }

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

    private String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return traceId != null ? traceId : "no-trace";
    }
}

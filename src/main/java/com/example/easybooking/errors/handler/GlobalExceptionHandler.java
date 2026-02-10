package com.example.easybooking.errors.handler;

import com.example.easybooking.common.filter.TraceIdFilter;
import com.example.easybooking.errors.errorcode.CommonErrorCode;
import com.example.easybooking.errors.errorcode.ErrorCode;
import com.example.easybooking.errors.exception.BaseBusinessException;
import com.example.easybooking.errors.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
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

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        return ResponseEntity.status(CommonErrorCode.INVALID_PARAMETER.getHttpStatus())
                .body(makeErrorResponse(
                        CommonErrorCode.INVALID_PARAMETER,
                        CommonErrorCode.INVALID_PARAMETER.getMessage(),
                        extractHttpServletRequest(request),
                        details
                ));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        // JSON 파싱 실패/타입 불일치 등
        var details = List.of("요청 본문(JSON) 형식이 올바르지 않습니다.");

        return ResponseEntity.status(CommonErrorCode.INVALID_PARAMETER.getHttpStatus())
                .body(makeErrorResponse(
                        CommonErrorCode.INVALID_PARAMETER,
                        CommonErrorCode.INVALID_PARAMETER.getMessage(),
                        extractHttpServletRequest(request),
                        details
                ));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        var details = List.of(ex.getParameterName() + ": 필수 파라미터가 누락되었습니다.");

        return ResponseEntity.status(CommonErrorCode.INVALID_PARAMETER.getHttpStatus())
                .body(makeErrorResponse(
                        CommonErrorCode.INVALID_PARAMETER,
                        CommonErrorCode.INVALID_PARAMETER.getMessage(),
                        extractHttpServletRequest(request),
                        details
                ));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        var details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());

        return ResponseEntity.status(CommonErrorCode.INVALID_PARAMETER.getHttpStatus())
                .body(makeErrorResponse(
                        CommonErrorCode.INVALID_PARAMETER,
                        CommonErrorCode.INVALID_PARAMETER.getMessage(),
                        request,
                        details
                ));
    }


    private String formatFieldError(FieldError e) {
        // 예: "message: 수락 시 고객에게 전달할 메시지는 필수 입력 사항입니다."
        return e.getField() + ": " + e.getDefaultMessage();
    }

    private jakarta.servlet.http.HttpServletRequest extractHttpServletRequest(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest();
        }
        return null;
    }


    private String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return traceId != null ? traceId : "no-trace";
    }
}

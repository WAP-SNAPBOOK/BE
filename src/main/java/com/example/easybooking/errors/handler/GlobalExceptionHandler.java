package com.example.easybooking.errors.handler;

import com.example.easybooking.errors.errorcode.CommonErrorCode;
import com.example.easybooking.errors.errorcode.ErrorCode;
import com.example.easybooking.errors.exception.BaseBusinessException;
import com.example.easybooking.errors.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
        log.error("[{}][{}] {}", request.getRequestURI(), e.getErrorCode().name(), e.getMessage(), e);
        return buildResponse(e.getErrorCode(), e.getDetailMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e,
                                                                   HttpServletRequest request) {
        log.error("[{}] Unexpected error {}", request.getRequestURI(), e.getMessage(), e);
        return buildResponse(CommonErrorCode.INTERNAL_SERVER_ERROR, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode,
                                                        String customMessage) {
        String message = customMessage != null ? customMessage : errorCode.getMessage();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(makeErrorResponse(errorCode, message));
    }

    private ErrorResponse makeErrorResponse(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(message)
                .build();
    }
}

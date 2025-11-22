package com.example.easybooking.errors.handler;

import com.example.easybooking.common.filter.TraceLoggingFilter;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.errorcode.ChatRoomErrorCode;
import com.example.easybooking.errors.errorcode.CommonErrorCode;
import com.example.easybooking.errors.errorcode.ErrorCode;
import com.example.easybooking.errors.errorcode.FileErrorCode;
import com.example.easybooking.errors.errorcode.FormErrorCode;
import com.example.easybooking.errors.errorcode.ReservationErrorCode;
import com.example.easybooking.errors.errorcode.ShopErrorCode;
import com.example.easybooking.errors.errorcode.SlotErrorCode;
import com.example.easybooking.errors.errorcode.UserErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.errors.exception.ChatException;
import com.example.easybooking.errors.exception.ChatRoomException;
import com.example.easybooking.errors.exception.FileException;
import com.example.easybooking.errors.exception.FormException;
import com.example.easybooking.errors.exception.ReservationException;
import com.example.easybooking.errors.exception.ShopException;
import com.example.easybooking.errors.exception.SlotException;
import com.example.easybooking.errors.exception.UserException;
import com.example.easybooking.errors.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e, HttpServletRequest request) {
        AuthErrorCode authErrorCode = e.getAuthErrorCode();
        return handleExceptionInternal(authErrorCode, e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<ErrorResponse> handleFileException(FileException e, HttpServletRequest request) {
        FileErrorCode fileErrorCode = e.getFileErrorCode();
        return handleExceptionInternal(fileErrorCode, e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException e, HttpServletRequest request) {
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return handleExceptionInternal(userErrorCode, e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler(ChatRoomException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomException(ChatRoomException e, HttpServletRequest request) {
        ChatRoomErrorCode chatRoomErrorCode = e.getChatRoomErrorCode();
        return handleExceptionInternal(chatRoomErrorCode, e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler(ShopException.class)
    public ResponseEntity<ErrorResponse> handleShopException(ShopException e, HttpServletRequest request) {
        ShopErrorCode shopErrorCode = e.getShopErrorCode();
        return handleExceptionInternal(shopErrorCode, e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorResponse> handleReservationException(ReservationException e, HttpServletRequest request) {
        ReservationErrorCode reservationErrorCode = e.getReservationErrorCode();
        return handleExceptionInternal(reservationErrorCode, e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatException(ChatException e, HttpServletRequest request) {
        return handleExceptionInternal(e.getChatErrorCode(), e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler(FormException.class)
    public ResponseEntity<ErrorResponse> handleFormException(FormException e, HttpServletRequest request) {
        FormErrorCode formErrorCode = e.getFormErrorCode();
        return handleExceptionInternal(formErrorCode, e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler(SlotException.class)
    public ResponseEntity<ErrorResponse> handleSlotException(SlotException e, HttpServletRequest request) {
        SlotErrorCode slotErrorCode = e.getSlotErrorCode();
        return handleExceptionInternal(slotErrorCode, e.getMessage(), request, e, Collections.emptyList());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                                     HttpServletRequest request) {
        return handleExceptionInternal(
                CommonErrorCode.INVALID_PARAMETER,
                e.getMessage(),
                request,
                e,
                Collections.singletonList(e.getName() + " : " + e.getMessage())
        );
    }

    @ExceptionHandler({jakarta.validation.ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            jakarta.validation.ConstraintViolationException e,
            HttpServletRequest request) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " : " + violation.getMessage())
                .toList();
        return handleExceptionInternal(CommonErrorCode.INVALID_PARAMETER, e.getMessage(), request, e, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e, HttpServletRequest request) {
        return handleExceptionInternal(
                CommonErrorCode.INTERNAL_SERVER_ERROR,
                CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                request,
                e,
                Collections.emptyList()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  org.springframework.http.HttpStatusCode status,
                                                                  WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " : " + fieldError.getDefaultMessage())
                .toList();
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        ErrorResponse errorResponse = makeErrorResponse(
                CommonErrorCode.INVALID_PARAMETER,
                CommonErrorCode.INVALID_PARAMETER.getMessage(),
                servletRequest,
                errors
        );
        log.warn("요청 유효성 검증 실패: path={}, errors={}", servletRequest.getRequestURI(), errors, ex);
        return ResponseEntity.status(CommonErrorCode.INVALID_PARAMETER.getHttpStatus()).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  org.springframework.http.HttpStatusCode status,
                                                                  WebRequest request) {
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        ErrorResponse errorResponse = makeErrorResponse(
                CommonErrorCode.INVALID_PARAMETER,
                CommonErrorCode.INVALID_PARAMETER.getMessage(),
                servletRequest,
                Collections.singletonList(Objects.toString(ex.getMostSpecificCause().getMessage(), ex.getMessage()))
        );
        log.warn("요청 본문 파싱 실패: path={}", servletRequest.getRequestURI(), ex);
        return ResponseEntity.status(CommonErrorCode.INVALID_PARAMETER.getHttpStatus()).body(errorResponse);
    }

    private ResponseEntity<ErrorResponse> handleExceptionInternal(ErrorCode errorCode,
                                                                  String message,
                                                                  HttpServletRequest request,
                                                                  Throwable throwable,
                                                                  List<String> errors) {
        ErrorResponse errorResponse = makeErrorResponse(errorCode, message, request, errors);
        log.error("API 예외 발생: path={}, code={}, message={}, traceId={}",
                request.getRequestURI(), errorCode.name(), message, errorResponse.getTraceId(), throwable);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(errorResponse);
    }

    private ErrorResponse makeErrorResponse(ErrorCode errorCode,
                                            String message,
                                            HttpServletRequest request,
                                            List<String> errors) {
        String resolvedMessage = StringUtils.hasText(message) ? message : errorCode.getMessage();

        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(resolvedMessage)
                .path(request.getRequestURI())
                .traceId(MDC.get(TraceLoggingFilter.TRACE_ID))
                .timestamp(Instant.now())
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }
}

package com.example.easybooking.errors.handler;

import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.errorcode.ChatRoomErrorCode;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {
        AuthErrorCode authErrorCode = e.getAuthErrorCode();
        return handleExceptionInternal(authErrorCode);
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<ErrorResponse> handleFileException(FileException e) {
        FileErrorCode fileErrorCode = e.getFileErrorCode();
        return handleExceptionInternal(fileErrorCode);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException e) {
        UserErrorCode userErrorCode = e.getUserErrorCode();
        return handleExceptionInternal(userErrorCode);
    }

    @ExceptionHandler(ChatRoomException.class)
    public ResponseEntity<ErrorResponse> handleChatRoomException(ChatRoomException e) {
        ChatRoomErrorCode chatRoomErrorCode = e.getChatRoomErrorCode();
        return handleExceptionInternal(chatRoomErrorCode);
    }

    @ExceptionHandler(ShopException.class)
    public ResponseEntity<ErrorResponse> handleShopException(ShopException e) {
        ShopErrorCode shopErrorCode = e.getShopErrorCode();
        return handleExceptionInternal(shopErrorCode);
    }

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorResponse> handleReservationException(ReservationException e) {
        ReservationErrorCode reservationErrorCode = e.getReservationErrorCode();
        return handleExceptionInternal(reservationErrorCode);
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatException(ChatException e) {
        return handleExceptionInternal(e.getChatErrorCode());
    }

    @ExceptionHandler(FormException.class)
    public ResponseEntity<ErrorResponse> handleFormException(FormException e) {
        FormErrorCode formErrorCode = e.getFormErrorCode();
        return handleExceptionInternal(formErrorCode);
    }

    @ExceptionHandler(SlotException.class)
    public ResponseEntity<ErrorResponse> handleSlotException(SlotException e) {
        SlotErrorCode slotErrorCode = e.getSlotErrorCode();
        return handleExceptionInternal(slotErrorCode);
    }


    public ResponseEntity<ErrorResponse> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(makeErrorResponse(errorCode));
    }

    public ErrorResponse makeErrorResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }
}

package com.example.easybooking.errors.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum FileErrorCode implements ErrorCode {
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패"),
    EMPTY_FILE(HttpStatus.BAD_REQUEST,"파일이 비어있습니다."),
    TOO_MANY_FILES(HttpStatus.BAD_REQUEST,"파일은 최대 5개까지 업로드 가능합니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST,"허용되지 않는 파일 형식입니다."),
    TOO_LARGE_FILE(HttpStatus.BAD_REQUEST,"파일당 최대 용량은 10MB입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}

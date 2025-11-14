package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.FileErrorCode;
import lombok.Getter;

@Getter
public class FileException extends RuntimeException {
    private final FileErrorCode fileErrorCode;

    public FileException(FileErrorCode errorCode) {
        super(errorCode.getMessage());
        this.fileErrorCode = errorCode;
    }
}

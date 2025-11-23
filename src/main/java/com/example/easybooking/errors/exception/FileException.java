package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.FileErrorCode;

public class FileException extends BaseBusinessException {

    public FileException(FileErrorCode errorCode) {
        super(errorCode);
    }

    public FileException(FileErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}

package com.example.easybooking.errors.exception;

import com.example.easybooking.errors.errorcode.FileErrorCode;

public class FileException extends BaseBusinessException {

    /**
     * Creates a FileException associated with the given file error code.
     *
     * @param errorCode the FileErrorCode describing the error condition
     */
    public FileException(FileErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Create a FileException associated with the specified file error code and additional detail message.
     *
     * @param errorCode     the FileErrorCode that categorizes the file-related error
     * @param detailMessage additional context or human-readable details to include with the error
     */
    public FileException(FileErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
package com.example.easybooking.file.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileUploadResponse {
    private String fileUrl;
    private String message;

    public FileUploadResponse(String fileUrl) {
        this.fileUrl = fileUrl;
        this.message = "업로드 성공";
    }

    public FileUploadResponse(String message, String error) {
        this.message = error;
        this.fileUrl = null;
    }
}
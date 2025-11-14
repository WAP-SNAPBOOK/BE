package com.example.easybooking.file.presentation;

import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.auth.annotation.RequireAuthenticatedUser;
import com.example.easybooking.file.dto.response.FileUploadResponse;
import com.example.easybooking.file.service.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final S3Service s3Service;

    /**
     * 단일 이미지 업로드
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequireAuthenticatedUser AuthenticatedUser user) {

        String fileUrl = s3Service.uploadImage(file, user.getUserId());
        return ResponseEntity.ok(new FileUploadResponse(fileUrl));
    }

    /**
     * 여러 이미지 업로드
     */
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileUploadResponse>> uploadFiles(
            @RequestPart("files") List<MultipartFile> files,
            @RequireAuthenticatedUser AuthenticatedUser user) {

        List<String> urls = s3Service.uploadImages(files, user.getUserId());
        List<FileUploadResponse> responses = urls.stream()
                .map(FileUploadResponse::new)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
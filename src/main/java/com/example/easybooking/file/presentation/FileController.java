package com.example.easybooking.file.presentation;

import com.example.easybooking.auth.AuthenticatedUser;
import com.example.easybooking.auth.RequireAuthenticatedUser;
import com.example.easybooking.file.dto.response.FileUploadResponse;
import com.example.easybooking.file.service.S3Service;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequireAuthenticatedUser AuthenticatedUser user) {

        try {
            String fileUrl = s3Service.uploadImage(file, user.getUserId());
            return ResponseEntity.ok(new FileUploadResponse(fileUrl));
        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FileUploadResponse("파일 업로드에 실패했습니다."));
        } catch (IllegalArgumentException e) {
            log.error("파일 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new FileUploadResponse(e.getMessage()));
        }
    }

    /**
     * 여러 이미지 업로드
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<FileUploadResponse>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequireAuthenticatedUser AuthenticatedUser user) {

        try {
            List<String> urls = s3Service.uploadImages(files, user.getUserId());
            List<FileUploadResponse> responses = urls.stream()
                    .map(FileUploadResponse::new)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
package com.example.easybooking.file.service;

import com.example.easybooking.errors.errorcode.FileErrorCode;
import com.example.easybooking.errors.exception.FileException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.base-url}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 이미지 파일을 S3에 업로드하고 URL을 반환합니다.
     */
    public String uploadImage(MultipartFile file, Long userId) {
        // 파일 검증
        validateImageFile(file);

        // 고유한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(userId, extension);

        // S3 업로드 경로 (messages/디렉토리에 저장)
        String s3Key = "messages/" + uniqueFilename;

        // 메타데이터 설정 및 업로드
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        try {
            // RequestBody로 변환
            RequestBody requestBody = RequestBody.fromInputStream(file.getInputStream(), file.getSize());

            // S3에 업로드
            s3Client.putObject(putObjectRequest, requestBody);

            // 업로드된 파일의 URL 반환
            String fileUrl = baseUrl + "/" + s3Key;
            log.info("S3 업로드 성공: {}", fileUrl);

            return fileUrl;
        } catch (Exception e) {
            throw new FileException(FileErrorCode.FILE_UPLOAD_FAILED);
        }

    }

    /**
     * 여러 이미지를 배치로 업로드합니다.
     */
    public List<String> uploadImages(List<MultipartFile> files, Long userId) {
        if (files.size() > 5) {
            throw new FileException(FileErrorCode.TOO_MANY_FILES);
        }
        return files.stream()
                .map(file -> uploadImage(file, userId))
                .toList();
    }

    /**
     * S3에서 파일을 삭제합니다.
     */
    public void deleteFile(String fileUrl) {
        try {
            // URL에서 S3 key 추출
            String s3Key = extractS3Key(fileUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("S3 파일 삭제 성공: {}", s3Key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {} - {}", fileUrl, e.getMessage());
        }
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new FileException(FileErrorCode.EMPTY_FILE);
        }
        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileException(FileErrorCode.TOO_LARGE_FILE);
        }

        // 확장자 확인
        String extension = getFileExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileException(FileErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 고유한 파일명 생성 형식: userId_timestamp_uuid.extension
     */
    private String generateUniqueFilename(Long userId, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%d_%s_%s.%s", userId, timestamp, uuid, extension);
    }

    /**
     * S3 URL에서 key 추출
     */
    private String extractS3Key(String fileUrl) {
        // https://bucket-name.s3.region.amazonaws.com/messages/filename.ext
        // -> messages/filename.ext
        return fileUrl.substring(baseUrl.length() + 1);
    }
}
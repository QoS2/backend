package com.app.questofseoul.service;

import com.app.questofseoul.config.S3Properties;
import com.app.questofseoul.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * S3 파일 업로드 서비스.
 * 오디오(audioUrl), 이미지(photoUrl) 업로드를 지원합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.s3.enabled", havingValue = "true")
public class FileUploadService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_AUDIO_TYPES = Set.of(
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/webm", "audio/m4a"
    );
    private static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50MB

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    /** 이미지: images/{category}/ (카테고리: tour, spot, mission 등) */
    public String uploadImage(MultipartFile file) {
        return uploadImage(file, "general");
    }

    public String uploadImage(MultipartFile file, String category) {
        validateFile(file, ALLOWED_IMAGE_TYPES, "이미지");
        return uploadFile(file, "images", sanitizeCategory(category));
    }

    /** 오디오: audio/{category}/ (카테고리: intro, ambient 등) */
    public String uploadAudio(MultipartFile file) {
        return uploadAudio(file, "general");
    }

    public String uploadAudio(MultipartFile file, String category) {
        validateFile(file, ALLOWED_AUDIO_TYPES, "오디오");
        return uploadFile(file, "audio", sanitizeCategory(category));
    }

    /**
     * 파일 타입에 따라 적절한 업로드 메서드를 호출합니다.
     * 이미지와 오디오 모두 지원합니다.
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    public String uploadFile(MultipartFile file, String category) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException("파일 타입을 확인할 수 없습니다.");
        }
        String cat = sanitizeCategory(category);
        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return uploadFile(file, "images", cat);
        }
        if (ALLOWED_AUDIO_TYPES.contains(contentType)) {
            return uploadFile(file, "audio", cat);
        }
        throw new BusinessException("지원하지 않는 파일 타입입니다. (이미지: jpeg/png/gif/webp, 오디오: mp3/wav/ogg/m4a)");
    }

    private String sanitizeCategory(String category) {
        if (category == null || category.isBlank()) return "general";
        String s = category.replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();
        return s.isEmpty() ? "general" : s;
    }

    private String uploadFile(MultipartFile file, String folder, String subFolder) {
        String bucket = s3Properties.getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new BusinessException("S3 버킷이 설정되지 않았습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String objectKey = folder + "/" + subFolder + "/" + UUID.randomUUID() + (extension != null ? "." + extension : "");

        try {
            // ACL 제거: "Bucket owner enforced" 모드는 객체별 ACL을 지원하지 않음.
            // 퍼블릭 읽기는 버킷 정책(Bucket Policy)으로 설정.
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

            s3Client.putObject(request,
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            String url = buildPublicUrl(objectKey);
            log.info("Uploaded file to S3: {} -> {}", objectKey, url);
            return url;
        } catch (IOException e) {
            log.error("S3 upload failed for {}", originalFilename, e);
            throw new BusinessException("파일 업로드에 실패했습니다.");
        }
    }

    private void validateFile(MultipartFile file, Set<String> allowedTypes, String typeLabel) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("파일이 비어 있습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException("파일 크기는 50MB 이하여야 합니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BusinessException("지원하지 않는 " + typeLabel + " 형식입니다.");
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String buildPublicUrl(String objectKey) {
        String region = s3Properties.getRegion();
        String bucket = s3Properties.getBucket();
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, objectKey);
    }
}

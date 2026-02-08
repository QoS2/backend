package com.app.questofseoul.controller;

import com.app.questofseoul.exception.BusinessException;
import com.app.questofseoul.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

/**
 * 파일 업로드 API.
 * S3가 활성화된 경우에만 동작합니다.
 * 이미지(photoUrl) 및 오디오(audioUrl) 업로드를 지원합니다.
 */
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "파일 업로드 (S3)")
public class FileUploadController {

    private final Optional<FileUploadService> fileUploadService;

    @Operation(summary = "파일 업로드",
        description = "이미지 또는 오디오 파일을 S3에 업로드하고 URL을 반환합니다. 인증 필요.")
    @SecurityRequirement(name = "bearerAuth")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            Authentication authentication,
            @Parameter(description = "업로드할 파일 (이미지: jpeg/png/gif/webp, 오디오: mp3/wav/ogg/m4a)")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "파일 타입 (optional: image | audio - 없으면 Content-Type으로 자동 판별)")
            @RequestParam(value = "type", required = false) String type) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        FileUploadService service = fileUploadService.orElseThrow(() ->
            new BusinessException("파일 업로드가 비활성화되어 있습니다. AWS S3 설정을 확인하세요."));

        String url;
        if ("image".equalsIgnoreCase(type)) {
            url = service.uploadImage(file);
        } else if ("audio".equalsIgnoreCase(type)) {
            url = service.uploadAudio(file);
        } else {
            url = service.uploadFile(file);
        }

        return ResponseEntity.ok(Map.of("url", url));
    }
}

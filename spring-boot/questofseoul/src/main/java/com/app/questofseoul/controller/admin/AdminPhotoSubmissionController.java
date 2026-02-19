package com.app.questofseoul.controller.admin;

import com.app.questofseoul.domain.entity.UserPhotoSubmission;
import com.app.questofseoul.domain.enums.PhotoSubmissionStatus;
import com.app.questofseoul.dto.admin.PhotoSubmissionVerifyRequest;
import com.app.questofseoul.service.AuthService;
import com.app.questofseoul.service.admin.AdminPhotoSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/photo-submissions")
@RequiredArgsConstructor
@Tag(name = "관리자 - 포토 검증", description = "포토 제출 검증 API")
public class AdminPhotoSubmissionController {

    private final AdminPhotoSubmissionService adminPhotoSubmissionService;
    private final AuthService authService;

    @Operation(summary = "검증 대기 목록")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(required = false, defaultValue = "PENDING") String status) {
        PhotoSubmissionStatus statusEnum = parseStatus(status);
        List<UserPhotoSubmission> list = adminPhotoSubmissionService.getSubmissions(statusEnum);
        List<Map<String, Object>> items = list.stream()
                .map(s -> Map.<String, Object>of(
                        "submissionId", s.getId(),
                        "spotId", s.getSpot().getId(),
                        "spotTitle", s.getSpot().getTitle(),
                        "photoUrl", s.getAsset().getUrl(),
                        "status", s.getStatus().name(),
                        "submittedAt", s.getSubmittedAt().toString(),
                        "userNickname", s.getUser().getNickname() != null ? s.getUser().getNickname() : ""
                ))
                .toList();
        return ResponseEntity.ok(items);
    }

    private PhotoSubmissionStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) return PhotoSubmissionStatus.PENDING;
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(normalized)) return null;
        try {
            return PhotoSubmissionStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("status must be one of: PENDING, APPROVED, REJECTED, ALL");
        }
    }

    @Operation(summary = "승인/거절")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{submissionId}")
    public ResponseEntity<Void> verify(
            @PathVariable Long submissionId,
            @Valid @RequestBody PhotoSubmissionVerifyRequest request) {
        UUID adminId = authService.getCurrentUserId();
        adminPhotoSubmissionService.verifySubmission(submissionId, request, adminId);
        return ResponseEntity.ok().build();
    }
}

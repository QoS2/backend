package com.app.questofseoul.controller;

import com.app.questofseoul.domain.enums.PhotoSubmissionStatus;
import com.app.questofseoul.dto.photo.MyPhotoSubmissionItemDto;
import com.app.questofseoul.dto.photo.PhotoSpotDetailResponse;
import com.app.questofseoul.dto.photo.PhotoSpotItemDto;
import com.app.questofseoul.dto.photo.PhotoSubmissionRequest;
import com.app.questofseoul.dto.photo.PhotoSubmissionResponse;
import com.app.questofseoul.service.AuthService;
import com.app.questofseoul.service.PhotoSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "포토 스팟", description = "Photo Spot API")
public class PhotoSpotController {

    private final PhotoSpotService photoSpotService;
    private final AuthService authService;

    @Operation(summary = "포토 스팟 목록")
    @GetMapping("/photo-spots")
    public ResponseEntity<List<PhotoSpotItemDto>> getPhotoSpots(
            @RequestParam(required = false) Long tourId,
            @RequestParam(required = false, defaultValue = "ko") String lang) {
        return ResponseEntity.ok(photoSpotService.getPhotoSpots(tourId));
    }

    @Operation(summary = "포토 스팟 상세 (유저 갤러리 포함)")
    @GetMapping("/photo-spots/{spotId}")
    public ResponseEntity<PhotoSpotDetailResponse> getPhotoSpotDetail(@PathVariable Long spotId) {
        return ResponseEntity.ok(photoSpotService.getPhotoSpotDetail(spotId));
    }

    @Operation(summary = "포토 제출")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/photo-spots/{spotId}/submissions")
    public ResponseEntity<PhotoSubmissionResponse> submitPhoto(
            @PathVariable Long spotId,
            @Valid @RequestBody PhotoSubmissionRequest request) {
        UUID userId = authService.getCurrentUserId();
        PhotoSubmissionResponse res = photoSpotService.submitPhoto(userId, spotId, request.photoUrl());
        return ResponseEntity.status(201).body(res);
    }

    @Operation(summary = "내 포토 제출 목록")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/photo-spots/my-submissions")
    public ResponseEntity<List<MyPhotoSubmissionItemDto>> getMySubmissions(
            @RequestParam(required = false) Long spotId,
            @RequestParam(required = false) @Parameter(description = "PENDING, APPROVED, REJECTED") String status) {
        UUID userId = authService.getCurrentUserId();
        PhotoSubmissionStatus statusEnum = status != null ? PhotoSubmissionStatus.valueOf(status) : null;
        return ResponseEntity.ok(photoSpotService.getMySubmissions(userId, spotId, statusEnum));
    }
}

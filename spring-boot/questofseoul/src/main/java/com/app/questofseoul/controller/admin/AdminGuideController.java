package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.GuideAdminResponse;
import com.app.questofseoul.dto.admin.GuideSaveRequest;
import com.app.questofseoul.service.admin.AdminGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tours/{tourId}/spots/{spotId}/guide")
@RequiredArgsConstructor
@Tag(name = "관리자 - Spot 가이드", description = "스팟 가이드 (문장 + 이미지/오디오) CRUD")
public class AdminGuideController {

    private final AdminGuideService adminGuideService;

    @Operation(summary = "가이드 조회", description = "스팟 가이드 step + script lines + assets")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<GuideAdminResponse> get(
            @PathVariable Long tourId,
            @PathVariable Long spotId) {
        return ResponseEntity.ok(adminGuideService.getGuide(tourId, spotId));
    }

    @Operation(summary = "가이드 저장", description = "가이드 전체 덮어쓰기. assets[].url은 S3 업로드 API로 먼저 업로드한 URL")
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping
    public ResponseEntity<GuideAdminResponse> save(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @Valid @RequestBody GuideSaveRequest request) {
        return ResponseEntity.ok(adminGuideService.saveGuide(tourId, spotId, request));
    }
}

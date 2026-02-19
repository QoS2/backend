package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.GuideStepsAdminResponse;
import com.app.questofseoul.dto.admin.GuideStepsSaveRequest;
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
@Tag(name = "관리자 - Spot 가이드", description = "스팟 가이드 (N개 컨텐츠 블록, 문장 + 이미지/오디오) CRUD")
public class AdminGuideController {

    private final AdminGuideService adminGuideService;

    @Operation(summary = "가이드 조회", description = "스팟 가이드 N개 step (컨텐츠 블록) + script lines + assets")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<GuideStepsAdminResponse> get(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @RequestParam(required = false, defaultValue = "ko") String lang) {
        return ResponseEntity.ok(adminGuideService.getGuideSteps(tourId, spotId, lang));
    }

    @Operation(summary = "가이드 저장", description = "가이드 전체 덮어쓰기 (N개 컨텐츠 블록). assets[].url은 S3 업로드 API로 먼저 업로드한 URL")
    @SecurityRequirement(name = "sessionAuth")
    @PutMapping
    public ResponseEntity<GuideStepsAdminResponse> save(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @Valid @RequestBody GuideStepsSaveRequest request) {
        return ResponseEntity.ok(adminGuideService.saveGuideSteps(tourId, spotId, request));
    }
}

package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.SpotAssetRequest;
import com.app.questofseoul.dto.admin.SpotAssetResponse;
import com.app.questofseoul.service.admin.AdminSpotAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tours/{tourId}/spots/{spotId}/assets")
@RequiredArgsConstructor
@Tag(name = "관리자 - Spot Assets", description = "스팟별 썸네일/히어로/갤러리 이미지 관리")
public class AdminSpotAssetController {

    private final AdminSpotAssetService adminSpotAssetService;

    @Operation(summary = "스팟 에셋 목록", description = "THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE 등")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<SpotAssetResponse>> list(
            @PathVariable Long tourId, @PathVariable Long spotId) {
        return ResponseEntity.ok(adminSpotAssetService.list(tourId, spotId));
    }

    @Operation(summary = "스팟 에셋 추가", description = "url은 S3 업로드 API로 먼저 업로드. usage: THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<SpotAssetResponse> add(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @Valid @RequestBody SpotAssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminSpotAssetService.add(tourId, spotId, request));
    }

    @Operation(summary = "스팟 에셋 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{spotAssetId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @PathVariable Long spotAssetId) {
        adminSpotAssetService.delete(tourId, spotId, spotAssetId);
        return ResponseEntity.noContent().build();
    }
}

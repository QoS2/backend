package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.TourAssetRequest;
import com.app.questofseoul.dto.admin.TourAssetResponse;
import com.app.questofseoul.service.admin.AdminTourAssetService;
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
@RequestMapping("/api/v1/admin/tours/{tourId}/assets")
@RequiredArgsConstructor
@Tag(name = "관리자 - Tour Assets", description = "투어 레벨 썸네일/이미지 관리")
public class AdminTourAssetController {

    private final AdminTourAssetService adminTourAssetService;

    @Operation(summary = "투어 에셋 목록", description = "THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<TourAssetResponse>> list(@PathVariable Long tourId) {
        return ResponseEntity.ok(adminTourAssetService.list(tourId));
    }

    @Operation(summary = "투어 에셋 추가", description = "url은 S3 업로드 API로 먼저 업로드. usage: THUMBNAIL, HERO_IMAGE, GALLERY_IMAGE")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<TourAssetResponse> add(
            @PathVariable Long tourId,
            @Valid @RequestBody TourAssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminTourAssetService.add(tourId, request));
    }

    @Operation(summary = "투어 에셋 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{tourAssetId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long tourId,
            @PathVariable Long tourAssetId) {
        adminTourAssetService.delete(tourId, tourAssetId);
        return ResponseEntity.noContent().build();
    }
}

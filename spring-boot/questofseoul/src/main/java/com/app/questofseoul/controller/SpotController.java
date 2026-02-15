package com.app.questofseoul.controller;

import com.app.questofseoul.dto.tour.GuideSegmentResponse;
import com.app.questofseoul.dto.tour.SpotDetailResponse;
import com.app.questofseoul.service.SpotGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "스팟", description = "스팟 가이드 등")
public class SpotController {

    private final SpotGuideService spotGuideService;

    @Operation(summary = "스팟 가이드", description = "스팟 페이지용 가이드 세그먼트 (설명 + 이미지)")
    @GetMapping("/spots/{spotId}/guide")
    public ResponseEntity<GuideSegmentResponse> getSpotGuide(
            @PathVariable Long spotId,
            @RequestParam(required = false) @Parameter(description = "ko, en, jp, cn") String lang) {
        return ResponseEntity.ok(spotGuideService.getSpotGuide(spotId, lang));
    }

    @Operation(summary = "스팟 상세", description = "Place/Treasure 더블모달용 상세 정보")
    @GetMapping("/spots/{spotId}")
    public ResponseEntity<SpotDetailResponse> getSpotDetail(@PathVariable Long spotId) {
        return ResponseEntity.ok(spotGuideService.getSpotDetail(spotId));
    }
}

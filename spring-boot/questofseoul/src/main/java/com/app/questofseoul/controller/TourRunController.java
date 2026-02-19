package com.app.questofseoul.controller;

import com.app.questofseoul.dto.tour.ChatSessionStatusResponse;
import com.app.questofseoul.dto.tour.NextSpotResponse;
import com.app.questofseoul.dto.tour.ProximityRequest;
import com.app.questofseoul.dto.tour.ProximityResponse;
import com.app.questofseoul.service.AuthService;
import com.app.questofseoul.service.ChatSessionService;
import com.app.questofseoul.service.CollectionService;
import com.app.questofseoul.service.ProximityService;
import com.app.questofseoul.service.TourRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "투어 Run", description = "Proximity 등 Run 진행 API")
public class TourRunController {

    private final ProximityService proximityService;
    private final ChatSessionService chatSessionService;
    private final CollectionService collectionService;
    private final TourRunService tourRunService;
    private final AuthService authService;

    @Operation(summary = "채팅 세션 획득", description = "run+spot에 대한 채팅 세션 ID 반환 (없으면 생성)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/tour-runs/{runId}/spots/{spotId}/chat-session")
    public ResponseEntity<ChatSessionStatusResponse> getOrCreateChatSession(
            @PathVariable Long runId,
            @PathVariable Long spotId) {
        java.util.UUID userId = authService.getCurrentUserId();
        ChatSessionStatusResponse response = chatSessionService.getOrCreateSessionStatus(userId, runId, spotId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "근접 감지", description = "50m 이내 스팟 진입 시 준비된 가이드 대사 반환")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/tour-runs/{runId}/proximity")
    public ResponseEntity<ProximityResponse> checkProximity(
            @PathVariable Long runId,
            @Valid @RequestBody ProximityRequest request,
            @RequestParam(required = false) @Parameter(description = "KO, EN, JP, CN") String lang) {
        UUID userId = authService.getCurrentUserId();
        ProximityResponse res = proximityService.checkProximity(
                userId, runId, request.lat(), request.lng(), lang);
        return res != null ? ResponseEntity.ok(res) : ResponseEntity.noContent().build();
    }

    @Operation(summary = "보물 수집", description = "Collect Treasure - 도감에 추가")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/tour-runs/{runId}/treasures/{spotId}/collect")
    public ResponseEntity<Void> collectTreasure(
            @PathVariable Long runId,
            @PathVariable Long spotId) {
        UUID userId = authService.getCurrentUserId();
        collectionService.collectTreasure(userId, runId, spotId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "다음 장소 조회", description = "Run 진행 기준 다음 MAIN/SUB 장소 반환")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/tour-runs/{runId}/next-spot")
    public ResponseEntity<NextSpotResponse> getNextSpot(@PathVariable Long runId) {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(tourRunService.getNextSpot(userId, runId));
    }
}

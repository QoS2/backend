package com.app.questofseoul.controller;

import com.app.questofseoul.dto.tour.ProximityRequest;
import com.app.questofseoul.dto.tour.ProximityResponse;
import com.app.questofseoul.service.AuthService;
import com.app.questofseoul.service.ChatSessionService;
import com.app.questofseoul.service.CollectionService;
import com.app.questofseoul.service.ProximityService;
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
    private final AuthService authService;

    @Operation(summary = "채팅 세션 획득", description = "run+spot에 대한 채팅 세션 ID 반환 (없으면 생성)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/tour-runs/{runId}/spots/{spotId}/chat-session")
    public ResponseEntity<java.util.Map<String, Long>> getOrCreateChatSession(
            @PathVariable Long runId,
            @PathVariable Long spotId) {
        java.util.UUID userId = authService.getCurrentUserId();
        Long sessionId = chatSessionService.getOrCreateSession(userId, runId, spotId);
        return ResponseEntity.ok(java.util.Map.of("sessionId", sessionId));
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
                userId, runId, request.latitude(), request.longitude(), lang);
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
}

package com.app.questofseoul.controller;

import com.app.questofseoul.dto.tour.MissionSubmitRequest;
import com.app.questofseoul.dto.tour.MissionSubmitResponse;
import com.app.questofseoul.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "미션", description = "미션 제출/채점")
public class MissionController {

    private final MissionService missionService;
    private final com.app.questofseoul.service.AuthService authService;

    @Operation(summary = "미션 제출", description = "run의 step에 연결된 미션 제출 및 채점")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/tour-runs/{runId}/steps/{stepId}/missions/submit")
    public ResponseEntity<MissionSubmitResponse> submitMission(
            @PathVariable Long runId,
            @PathVariable Long stepId,
            @Valid @RequestBody MissionSubmitRequest request) {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(missionService.submitMission(userId, runId, stepId, request));
    }
}

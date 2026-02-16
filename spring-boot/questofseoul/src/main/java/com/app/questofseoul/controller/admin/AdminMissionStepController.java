package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.MissionStepCreateRequest;
import com.app.questofseoul.dto.admin.MissionStepResponse;
import com.app.questofseoul.dto.admin.MissionStepUpdateRequest;
import com.app.questofseoul.service.admin.AdminMissionStepService;
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
@RequestMapping("/api/v1/admin/tours/{tourId}/spots/{spotId}/mission-steps")
@RequiredArgsConstructor
@Tag(name = "관리자 - Mission Step", description = "MISSION 스텝 CRUD")
public class AdminMissionStepController {

    private final AdminMissionStepService adminMissionStepService;

    @Operation(summary = "MISSION 스텝 목록")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<MissionStepResponse>> list(
            @PathVariable Long tourId, @PathVariable Long spotId) {
        return ResponseEntity.ok(adminMissionStepService.list(tourId, spotId));
    }

    @Operation(summary = "MISSION 스텝 추가")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<MissionStepResponse> create(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @Valid @RequestBody MissionStepCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminMissionStepService.create(tourId, spotId, request));
    }

    @Operation(summary = "MISSION 스텝 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{stepId}")
    public ResponseEntity<MissionStepResponse> update(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @PathVariable Long stepId,
            @RequestBody MissionStepUpdateRequest request) {
        return ResponseEntity.ok(adminMissionStepService.update(tourId, spotId, stepId, request));
    }

    @Operation(summary = "MISSION 스텝 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{stepId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long tourId,
            @PathVariable Long spotId,
            @PathVariable Long stepId) {
        adminMissionStepService.delete(tourId, spotId, stepId);
        return ResponseEntity.noContent().build();
    }
}

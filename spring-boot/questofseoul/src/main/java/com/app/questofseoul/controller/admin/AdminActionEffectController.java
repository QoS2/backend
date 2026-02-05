package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.EffectCreateRequest;
import com.app.questofseoul.dto.admin.EffectResponse;
import com.app.questofseoul.dto.admin.EffectUpdateRequest;
import com.app.questofseoul.service.admin.AdminEffectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions/{actionId}/effects")
@RequiredArgsConstructor
@Tag(name = "관리자 - 액션 이펙트", description = "액션 이펙트 CRUD")
public class AdminActionEffectController {

    private final AdminEffectService adminEffectService;

    @Operation(summary = "이펙트 목록 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<EffectResponse>> list(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID actionId) {
        return ResponseEntity.ok(adminEffectService.listByAction(questId, nodeId, actionId));
    }

    @Operation(summary = "이펙트 단건 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{effectId}")
    public ResponseEntity<EffectResponse> get(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID actionId,
            @Parameter(description = "이펙트 ID") @PathVariable UUID effectId) {
        return ResponseEntity.ok(adminEffectService.get(questId, nodeId, actionId, effectId));
    }

    @Operation(summary = "이펙트 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<EffectResponse> create(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID actionId,
            @Valid @RequestBody EffectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminEffectService.create(questId, nodeId, actionId, request));
    }

    @Operation(summary = "이펙트 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{effectId}")
    public ResponseEntity<EffectResponse> update(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID actionId,
            @PathVariable UUID effectId,
            @RequestBody EffectUpdateRequest request) {
        return ResponseEntity.ok(adminEffectService.update(questId, nodeId, actionId, effectId, request));
    }

    @Operation(summary = "이펙트 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{effectId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID actionId,
            @PathVariable UUID effectId) {
        adminEffectService.delete(questId, nodeId, actionId, effectId);
        return ResponseEntity.noContent().build();
    }
}

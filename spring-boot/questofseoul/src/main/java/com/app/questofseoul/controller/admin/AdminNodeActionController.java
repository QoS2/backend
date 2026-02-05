package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.ActionCreateRequest;
import com.app.questofseoul.dto.admin.ActionResponse;
import com.app.questofseoul.dto.admin.ActionUpdateRequest;
import com.app.questofseoul.service.admin.AdminActionService;
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
@RequestMapping("/api/v1/admin/quests/{questId}/nodes/{nodeId}/actions")
@RequiredArgsConstructor
@Tag(name = "관리자 - 노드 액션", description = "노드 액션 CRUD")
public class AdminNodeActionController {

    private final AdminActionService adminActionService;

    @Operation(summary = "액션 목록 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<ActionResponse>> list(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId) {
        return ResponseEntity.ok(adminActionService.listByNode(questId, nodeId));
    }

    @Operation(summary = "액션 단건 조회 (이펙트 포함)")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{actionId}")
    public ResponseEntity<ActionResponse> get(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @Parameter(description = "액션 ID") @PathVariable UUID actionId,
            @Parameter(description = "이펙트 포함 여부") @RequestParam(defaultValue = "true") boolean includeEffects) {
        return ResponseEntity.ok(adminActionService.get(questId, nodeId, actionId, includeEffects));
    }

    @Operation(summary = "액션 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<ActionResponse> create(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @Valid @RequestBody ActionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminActionService.create(questId, nodeId, request));
    }

    @Operation(summary = "액션 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{actionId}")
    public ResponseEntity<ActionResponse> update(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID actionId,
            @RequestBody ActionUpdateRequest request) {
        return ResponseEntity.ok(adminActionService.update(questId, nodeId, actionId, request));
    }

    @Operation(summary = "액션 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{actionId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID actionId) {
        adminActionService.delete(questId, nodeId, actionId);
        return ResponseEntity.noContent().build();
    }
}

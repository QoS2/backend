package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.NodeCreateRequest;
import com.app.questofseoul.dto.admin.NodeResponse;
import com.app.questofseoul.dto.admin.NodeReorderRequest;
import com.app.questofseoul.dto.admin.NodeUpdateRequest;
import com.app.questofseoul.service.admin.AdminNodeService;
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
@RequestMapping("/api/v1/admin/quests/{questId}/nodes")
@RequiredArgsConstructor
@Tag(name = "관리자 - 퀘스트 노드", description = "노드 CRUD 및 순서 변경")
public class AdminQuestNodeController {

    private final AdminNodeService adminNodeService;

    @Operation(summary = "노드 목록 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<NodeResponse>> list(@PathVariable UUID questId) {
        return ResponseEntity.ok(adminNodeService.listByQuest(questId));
    }

    @Operation(summary = "노드 단건 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{nodeId}")
    public ResponseEntity<NodeResponse> get(
            @PathVariable UUID questId,
            @Parameter(description = "노드 ID") @PathVariable UUID nodeId) {
        return ResponseEntity.ok(adminNodeService.get(questId, nodeId));
    }

    @Operation(summary = "노드 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<NodeResponse> create(
            @PathVariable UUID questId,
            @Valid @RequestBody NodeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminNodeService.create(questId, request));
    }

    @Operation(summary = "노드 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{nodeId}")
    public ResponseEntity<NodeResponse> update(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @RequestBody NodeUpdateRequest request) {
        return ResponseEntity.ok(adminNodeService.update(questId, nodeId, request));
    }

    @Operation(summary = "노드 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{nodeId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId) {
        adminNodeService.delete(questId, nodeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "노드 순서 일괄 변경")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(
            @PathVariable UUID questId,
            @Valid @RequestBody NodeReorderRequest request) {
        adminNodeService.reorder(questId, request);
        return ResponseEntity.ok().build();
    }
}

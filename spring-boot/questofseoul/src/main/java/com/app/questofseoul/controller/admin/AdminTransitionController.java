package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.TransitionCreateRequest;
import com.app.questofseoul.dto.admin.TransitionResponse;
import com.app.questofseoul.dto.admin.TransitionUpdateRequest;
import com.app.questofseoul.service.admin.AdminTransitionService;
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
@RequestMapping("/api/v1/admin/quests/{questId}/transitions")
@RequiredArgsConstructor
@Tag(name = "관리자 - 노드 전환", description = "노드 전환 CRUD 및 목록")
public class AdminTransitionController {

    private final AdminTransitionService adminTransitionService;

    @Operation(summary = "퀘스트 내 전환 목록 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<TransitionResponse>> listByQuest(@PathVariable UUID questId) {
        return ResponseEntity.ok(adminTransitionService.listByQuest(questId));
    }

    @Operation(summary = "전환 단건 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{transitionId}")
    public ResponseEntity<TransitionResponse> get(
            @PathVariable UUID questId,
            @Parameter(description = "전환 ID") @PathVariable UUID transitionId) {
        return ResponseEntity.ok(adminTransitionService.get(questId, transitionId));
    }

    @Operation(summary = "전환 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<TransitionResponse> create(
            @PathVariable UUID questId,
            @Valid @RequestBody TransitionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminTransitionService.create(questId, request));
    }

    @Operation(summary = "전환 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{transitionId}")
    public ResponseEntity<TransitionResponse> update(
            @PathVariable UUID questId,
            @PathVariable UUID transitionId,
            @RequestBody TransitionUpdateRequest request) {
        return ResponseEntity.ok(adminTransitionService.update(questId, transitionId, request));
    }

    @Operation(summary = "전환 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{transitionId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID questId,
            @PathVariable UUID transitionId) {
        adminTransitionService.delete(questId, transitionId);
        return ResponseEntity.noContent().build();
    }
}

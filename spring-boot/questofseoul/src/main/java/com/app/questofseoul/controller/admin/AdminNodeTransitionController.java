package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.TransitionResponse;
import com.app.questofseoul.service.admin.AdminTransitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/quests/{questId}/nodes/{nodeId}/transitions")
@RequiredArgsConstructor
@Tag(name = "관리자 - 노드별 전환", description = "노드 기준 나가는/들어오는 전환 목록")
public class AdminNodeTransitionController {

    private final AdminTransitionService adminTransitionService;

    @Operation(summary = "해당 노드에서 나가는 전환 목록")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/outgoing")
    public ResponseEntity<List<TransitionResponse>> listOutgoing(
            @PathVariable UUID questId,
            @Parameter(description = "노드 ID") @PathVariable UUID nodeId) {
        return ResponseEntity.ok(adminTransitionService.listOutgoing(questId, nodeId));
    }

    @Operation(summary = "해당 노드로 들어오는 전환 목록")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/incoming")
    public ResponseEntity<List<TransitionResponse>> listIncoming(
            @PathVariable UUID questId,
            @Parameter(description = "노드 ID") @PathVariable UUID nodeId) {
        return ResponseEntity.ok(adminTransitionService.listIncoming(questId, nodeId));
    }
}

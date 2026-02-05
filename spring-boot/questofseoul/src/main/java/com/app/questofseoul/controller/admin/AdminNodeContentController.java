package com.app.questofseoul.controller.admin;

import com.app.questofseoul.dto.admin.ContentCreateRequest;
import com.app.questofseoul.dto.admin.ContentResponse;
import com.app.questofseoul.dto.admin.ContentUpdateRequest;
import com.app.questofseoul.service.admin.AdminContentService;
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
@RequestMapping("/api/v1/admin/quests/{questId}/nodes/{nodeId}/contents")
@RequiredArgsConstructor
@Tag(name = "관리자 - 노드 콘텐츠", description = "노드 콘텐츠 CRUD")
public class AdminNodeContentController {

    private final AdminContentService adminContentService;

    @Operation(summary = "콘텐츠 목록 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<List<ContentResponse>> list(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId) {
        return ResponseEntity.ok(adminContentService.listByNode(questId, nodeId));
    }

    @Operation(summary = "콘텐츠 단건 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{contentId}")
    public ResponseEntity<ContentResponse> get(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @Parameter(description = "콘텐츠 ID") @PathVariable UUID contentId) {
        return ResponseEntity.ok(adminContentService.get(questId, nodeId, contentId));
    }

    @Operation(summary = "콘텐츠 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<ContentResponse> create(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @Valid @RequestBody ContentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContentService.create(questId, nodeId, request));
    }

    @Operation(summary = "콘텐츠 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{contentId}")
    public ResponseEntity<ContentResponse> update(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID contentId,
            @RequestBody ContentUpdateRequest request) {
        return ResponseEntity.ok(adminContentService.update(questId, nodeId, contentId, request));
    }

    @Operation(summary = "콘텐츠 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID questId,
            @PathVariable UUID nodeId,
            @PathVariable UUID contentId) {
        adminContentService.delete(questId, nodeId, contentId);
        return ResponseEntity.noContent().build();
    }
}

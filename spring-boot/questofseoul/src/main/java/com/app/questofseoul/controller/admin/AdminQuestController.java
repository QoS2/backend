package com.app.questofseoul.controller.admin;

import com.app.questofseoul.domain.enums.QuestTheme;
import com.app.questofseoul.dto.QuestResponse;
import com.app.questofseoul.dto.admin.QuestCreateRequest;
import com.app.questofseoul.dto.admin.QuestUpdateRequest;
import com.app.questofseoul.service.admin.AdminQuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/quests")
@RequiredArgsConstructor
@Tag(name = "관리자 - 퀘스트", description = "퀘스트 CRUD 및 목록/필터/페이징")
public class AdminQuestController {

    private final AdminQuestService adminQuestService;

    @Operation(summary = "퀘스트 목록 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping
    public ResponseEntity<Page<QuestResponse>> list(
            @Parameter(description = "활성 여부") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "테마") @RequestParam(required = false) QuestTheme theme,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminQuestService.list(isActive, theme, pageable));
    }

    @Operation(summary = "퀘스트 단건 조회")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{questId}")
    public ResponseEntity<QuestResponse> get(
            @Parameter(description = "퀘스트 ID") @PathVariable UUID questId) {
        return adminQuestService.get(questId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "퀘스트 생성")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping
    public ResponseEntity<QuestResponse> create(@Valid @RequestBody QuestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminQuestService.create(request));
    }

    @Operation(summary = "퀘스트 수정")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{questId}")
    public ResponseEntity<QuestResponse> update(
            @PathVariable UUID questId,
            @RequestBody QuestUpdateRequest request) {
        return ResponseEntity.ok(adminQuestService.update(questId, request));
    }

    @Operation(summary = "퀘스트 삭제")
    @SecurityRequirement(name = "sessionAuth")
    @DeleteMapping("/{questId}")
    public ResponseEntity<Void> delete(@PathVariable UUID questId) {
        adminQuestService.delete(questId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "퀘스트 활성/비활성 토글")
    @SecurityRequirement(name = "sessionAuth")
    @PatchMapping("/{questId}/active")
    public ResponseEntity<QuestResponse> setActive(
            @PathVariable UUID questId,
            @Parameter(description = "활성 여부") @RequestParam boolean active) {
        return ResponseEntity.ok(adminQuestService.setActive(questId, active));
    }
}

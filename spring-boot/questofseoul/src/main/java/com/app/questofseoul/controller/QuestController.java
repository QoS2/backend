package com.app.questofseoul.controller;

import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.dto.*;
import com.app.questofseoul.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "퀘스트", description = "퀘스트 관리 및 진행 관련 API")
public class QuestController {

    private final QuestService questService;
    private final QuestStateService stateService;
    private final NodeService nodeService;
    private final ContentService contentService;
    private final ActionService actionService;
    private final HistoryService historyService;
    private final TransitionService transitionService;

    @Operation(summary = "활성 퀘스트 목록 조회", description = "모든 활성화된 퀘스트 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<QuestResponse>> getActiveQuests() {
        return ResponseEntity.ok(questService.getActiveQuests());
    }

    @Operation(summary = "퀘스트 상세 조회", description = "특정 퀘스트의 상세 정보를 조회합니다.")
    @GetMapping("/{questId}")
    public ResponseEntity<QuestResponse> getQuest(
            @Parameter(description = "퀘스트 ID") @PathVariable UUID questId) {
        return questService.getQuest(questId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "도착 확인", description = "사용자가 퀘스트 시작 장소로부터 100미터 이내에 도착했는지 확인합니다.")
    @PostMapping("/{questId}/check-arrival")
    public ResponseEntity<ArrivalCheckResponse> checkArrival(
            @Parameter(description = "퀘스트 ID") @PathVariable UUID questId,
            @RequestBody ArrivalCheckRequest request) {
        ArrivalCheckResponse response = questService.checkArrival(questId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "퀘스트 시작", description = "사용자에 대한 퀘스트를 초기화합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping("/{questId}/start")
    public ResponseEntity<Void> startQuest(
            Authentication authentication,
            @Parameter(description = "퀘스트 ID") @PathVariable UUID questId) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Starting quest: {} for user: {}", questId, userId);
        stateService.startQuest(userId, questId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "현재 노드 조회", description = "사용자의 퀘스트 진행 상황에 대한 현재 노드 메타데이터를 조회합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{questId}/current-node")
    public ResponseEntity<CurrentNodeResponse> getCurrentNode(
            Authentication authentication,
            @Parameter(description = "퀘스트 ID") @PathVariable UUID questId) {
        UUID userId = (UUID) authentication.getPrincipal();
        Optional<CurrentNodeResponse> response = nodeService.getCurrentNode(userId, questId);
        return response.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "노드 콘텐츠 조회", description = "특정 노드의 모든 콘텐츠를 조회합니다.")
    @GetMapping("/nodes/{nodeId}/content")
    public ResponseEntity<NodeContentsResponse> getNodeContent(
            @Parameter(description = "노드 ID") @PathVariable UUID nodeId,
            @Parameter(description = "언어 코드") @RequestParam(defaultValue = "KO") Language lang) {
        Optional<NodeContentsResponse> response = nodeService.getNodeContents(nodeId, lang);
        return response.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "현재 노드 콘텐츠 조회", description = "사용자의 현재 노드에 대한 콘텐츠를 조회합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{questId}/current-node/content")
    public ResponseEntity<NodeContentsResponse> getCurrentNodeContent(
            Authentication authentication,
            @Parameter(description = "퀘스트 ID") @PathVariable UUID questId,
            @Parameter(description = "언어 코드") @RequestParam(defaultValue = "KO") Language lang) {
        UUID userId = (UUID) authentication.getPrincipal();
        Optional<NodeContentsResponse> response = nodeService.getCurrentNodeContents(userId, questId, lang);
        return response.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "콘텐츠 소비 완료", description = "콘텐츠 항목을 소비 완료로 표시합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping("/nodes/{nodeId}/content-complete")
    public ResponseEntity<ContentCompleteResponse> completeContent(
            Authentication authentication,
            @Parameter(description = "노드 ID") @PathVariable UUID nodeId,
            @Parameter(description = "퀘스트 ID") @RequestParam UUID questId,
            @Parameter(description = "콘텐츠 ID") @RequestParam UUID contentId,
            @Parameter(description = "콘텐츠 순서") @RequestParam Integer contentOrder) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Completing content for user: {}, node: {}, content: {}", userId, nodeId, contentId);
        ContentCompleteResponse response = contentService.completeContent(
            userId, questId, nodeId, contentId, contentOrder);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "액션 제출", description = "사용자의 액션 응답을 제출합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping("/nodes/{nodeId}/actions/{actionId}/submit")
    public ResponseEntity<ActionSubmitResponse> submitAction(
            Authentication authentication,
            @Parameter(description = "노드 ID") @PathVariable UUID nodeId,
            @Parameter(description = "액션 ID") @PathVariable UUID actionId,
            @Parameter(description = "퀘스트 ID") @RequestParam UUID questId,
            @RequestBody ActionSubmitRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();
        ActionSubmitResponse response = actionService.submitAction(
            userId, questId, nodeId, actionId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "퀘스트 진행 내역 조회", description = "사용자의 퀘스트 내 모든 액션의 완전한 내역을 조회합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @GetMapping("/{questId}/history")
    public ResponseEntity<QuestHistoryResponse> getQuestHistory(
            Authentication authentication,
            @Parameter(description = "퀘스트 ID") @PathVariable UUID questId) {
        UUID userId = (UUID) authentication.getPrincipal();
        Optional<QuestHistoryResponse> response = historyService.getQuestHistory(userId, questId);
        return response.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "노드 액션 목록 조회", description = "노드에서 사용 가능한 모든 액션을 조회합니다.")
    @GetMapping("/nodes/{nodeId}/actions")
    public ResponseEntity<NodeActionsResponse> getNodeActions(
            @Parameter(description = "노드 ID") @PathVariable UUID nodeId) {
        Optional<NodeActionsResponse> response = actionService.getNodeActions(nodeId);
        return response.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "이동 메시지 조회", description = "두 노드 간의 이동 메시지를 조회합니다.")
    @GetMapping("/nodes/{fromNodeId}/transition/{toNodeId}")
    public ResponseEntity<TransitionMessageResponse> getTransitionMessages(
            @Parameter(description = "출발 노드 ID") @PathVariable UUID fromNodeId,
            @Parameter(description = "도착 노드 ID") @PathVariable UUID toNodeId,
            @Parameter(description = "언어 코드") @RequestParam(defaultValue = "KO") Language lang) {
        Optional<TransitionMessageResponse> response = transitionService.getTransitionMessages(
            fromNodeId, toNodeId, lang);
        return response.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "퀘스트 완료", description = "사용자의 퀘스트를 완료로 표시합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping("/{questId}/complete")
    public ResponseEntity<QuestCompletionResponse> completeQuest(
            Authentication authentication,
            @Parameter(description = "퀘스트 ID") @PathVariable UUID questId) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Completing quest: {} for user: {}", questId, userId);
        QuestCompletionResponse response = stateService.completeQuest(userId, questId);
        return ResponseEntity.ok(response);
    }
}

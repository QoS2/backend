package com.app.questofseoul.controller;

import com.app.questofseoul.dto.tour.ChatMessageRequest;
import com.app.questofseoul.dto.tour.ChatTurnsResponse;
import com.app.questofseoul.dto.tour.ProximityResponse;
import com.app.questofseoul.dto.tour.SendMessageResponse;
import com.app.questofseoul.service.ChatSessionService;
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
@Tag(name = "채팅", description = "AI Tour Guide 채팅")
public class ChatController {

    private final ChatSessionService chatSessionService;
    private final com.app.questofseoul.service.AuthService authService;

    @Operation(summary = "채팅 히스토리")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/chat-sessions/{sessionId}/turns")
    public ResponseEntity<ChatTurnsResponse> getChatTurns(@PathVariable Long sessionId) {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(chatSessionService.getChatTurns(userId, sessionId));
    }

    @Operation(summary = "다음 스크립트 턴 조회", description = "nextApi로 전달된 turnId 기준 단일 스크립트 턴 반환")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/chat-sessions/{sessionId}/turns/{nextTurnId}")
    public ResponseEntity<ProximityResponse.ChatTurnDto> getNextScriptTurn(
            @PathVariable Long sessionId,
            @PathVariable Long nextTurnId) {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(chatSessionService.getNextScriptTurn(userId, sessionId, nextTurnId));
    }

    @Operation(summary = "채팅 메시지 전송", description = "유저 질문 전송 후 AI 응답 반환")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/chat-sessions/{sessionId}/messages")
    public ResponseEntity<SendMessageResponse> sendMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody ChatMessageRequest request) {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(chatSessionService.sendMessage(userId, sessionId, request.text()));
    }
}

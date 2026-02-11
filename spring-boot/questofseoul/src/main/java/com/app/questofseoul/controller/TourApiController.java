package com.app.questofseoul.controller;

import com.app.questofseoul.domain.entity.User;
import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.domain.enums.MarkerType;
import com.app.questofseoul.dto.tour.*;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.UserRepository;
import com.app.questofseoul.service.AuthService;
import com.app.questofseoul.service.TourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "투어", description = "Tour 기반 가이드/채팅/퀴즈 API")
public class TourApiController {

    private final TourService tourService;
    private final AuthService authService;
    private final UserRepository userRepository;
    @Operation(summary = "투어 목록")
    @GetMapping("/tours")
    public ResponseEntity<List<com.app.questofseoul.dto.tour.TourListItem>> listTours() {
        return ResponseEntity.ok(tourService.listTours());
    }

    @Operation(summary = "투어 상세", description = "퀘스트 디테일 페이지용 - 태그, Place/게임/보물/포토 수, 설명, info, good_to_know")
    @GetMapping("/tours/{tourId}")
    public ResponseEntity<TourDetailResponse> getTourDetail(@PathVariable Long tourId) {
        UUID uid = getUserIdOrNull();
        return ResponseEntity.ok(tourService.getTourDetail(tourId, uid));
    }

    @Operation(summary = "마커 목록", description = "맵용 마커 (Place/Sub Place/Photo/Treasure). filter로 타입 필터 가능")
    @GetMapping("/tours/{tourId}/markers")
    public ResponseEntity<List<MarkerResponse>> getMarkers(
            @PathVariable Long tourId,
            @RequestParam(required = false) MarkerType filter) {
        return ResponseEntity.ok(tourService.getMarkers(tourId, filter));
    }

    @Operation(summary = "투어 시작")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/tours/{tourId}/start")
    public ResponseEntity<StartTourResponse> startTour(@PathVariable Long tourId) {
        UUID userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        StartTourResponse res = tourService.startTour(user, tourId);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "근접 감지", description = "50m 이내 마커 진입 시 준비된 대사 반환")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/tour-runs/{tourRunId}/proximity")
    public ResponseEntity<ProximityResponse> checkProximity(
            @PathVariable Long tourRunId,
            @Valid @RequestBody ProximityRequest request,
            @RequestParam(required = false) Language lang) {
        UUID userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ProximityResponse res = tourService.checkProximity(tourRunId, user, request.latitude(), request.longitude(), lang);
        return res != null ? ResponseEntity.ok(res) : ResponseEntity.noContent().build();
    }

    @Operation(summary = "채팅 히스토리")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/chat-sessions/{sessionId}/turns")
    public ResponseEntity<ChatTurnsResponse> getChatTurns(@PathVariable Long sessionId) {
        UUID userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(tourService.getChatTurns(sessionId, user));
    }

    @Operation(summary = "채팅 메시지 전송 (유저 질문 + AI 응답)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/chat-sessions/{sessionId}/messages")
    public ResponseEntity<SendMessageResponse> sendChatMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody ChatMessageRequest request) {
        UUID userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(tourService.sendUserMessage(sessionId, user, request.text()));
    }

    @Operation(summary = "스텝 가이드", description = "스텝 페이지용 가이드 세그먼트 (설명 + 이미지)")
    @GetMapping("/steps/{stepId}/guide")
    public ResponseEntity<GuideSegmentResponse> getStepGuide(
            @PathVariable Long stepId,
            @RequestParam(required = false) Language lang) {
        return ResponseEntity.ok(tourService.getStepGuide(stepId, lang));
    }

    @Operation(summary = "스텝 퀴즈 목록")
    @GetMapping("/steps/{stepId}/quizzes")
    public ResponseEntity<List<QuizResponse>> getStepQuizzes(@PathVariable Long stepId) {
        return ResponseEntity.ok(tourService.getStepQuizzes(stepId));
    }

    private UUID getUserIdOrNull() {
        try {
            return authService.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}

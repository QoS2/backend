package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.ChatSession;
import com.app.questofseoul.domain.entity.ChatTurn;
import com.app.questofseoul.domain.enums.ChatRole;
import com.app.questofseoul.domain.enums.ChatSource;
import com.app.questofseoul.dto.tour.ChatTurnsResponse;
import com.app.questofseoul.dto.tour.SendMessageResponse;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.ChatSessionRepository;
import com.app.questofseoul.repository.ChatTurnRepository;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.TourRunRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatTurnRepository chatTurnRepository;
    private final TourGuideAiService tourGuideAiService;
    private final TourRepository tourRepository;
    private final TourRunRepository tourRunRepository;
    private final TourSpotRepository tourSpotRepository;

    @Transactional(readOnly = true)
    public ChatTurnsResponse getChatTurns(java.util.UUID userId, Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        if (!session.getTourRun().getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your chat session");
        }
        List<ChatTurn> turns = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
        List<ChatTurnsResponse.ChatTurnItem> items = turns.stream()
                .map(t -> new ChatTurnsResponse.ChatTurnItem(
                        t.getId(), t.getRole().name(), t.getSource().name(), t.getText(),
                        null, null, t.getCreatedAt() != null ? t.getCreatedAt().toString() : null))
                .toList();
        return new ChatTurnsResponse(session.getId(), items);
    }

    @Transactional
    public SendMessageResponse sendMessage(java.util.UUID userId, Long sessionId, String text) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        if (!session.getTourRun().getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your chat session");
        }

        ChatTurn userTurn = ChatTurn.create(session, ChatSource.USER, ChatRole.USER, text);
        userTurn = chatTurnRepository.save(userTurn);

        String tourContext = buildTourContext(session.getTourRun().getTour().getId(),
                session.getSpot().getId());
        List<Map<String, String>> history = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(t -> Map.<String, String>of(
                        "role", t.getRole() == ChatRole.USER ? "user" : "assistant",
                        "content", t.getText() != null ? t.getText() : ""))
                .toList();
        String aiText = tourGuideAiService.generateResponse(tourContext, history);

        ChatTurn llmTurn = ChatTurn.create(session, ChatSource.LLM, ChatRole.GUIDE, aiText);
        llmTurn = chatTurnRepository.save(llmTurn);

        return new SendMessageResponse(userTurn.getId(), text, llmTurn.getId(), aiText);
    }

    @Transactional
    public Long getOrCreateSession(java.util.UUID userId, Long runId, Long spotId) {
        var run = tourRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }
        var spot = tourSpotRepository.findByIdAndTourId(spotId, run.getTour().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Spot not in tour"));
        return chatSessionRepository.findByTourRunIdAndSpotId(runId, spotId)
                .map(ChatSession::getId)
                .orElseGet(() -> chatSessionRepository.save(ChatSession.create(run, spot)).getId());
    }

    private String buildTourContext(Long tourId, Long spotId) {
        var tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("투어: ").append(tour.getDisplayTitle()).append("\n");
        if (tour.getDisplayDescription() != null) sb.append("설명: ").append(tour.getDisplayDescription()).append("\n");
        var spots = tourSpotRepository.findByTourIdOrderByOrderIndexAsc(tourId);
        for (var s : spots) {
            sb.append("- 스팟 ").append(s.getOrderIndex()).append(": ").append(s.getTitle());
            if (s.getDescription() != null) sb.append(" - ").append(s.getDescription());
            sb.append("\n");
        }
        return sb.toString();
    }
}

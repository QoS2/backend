package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.ChatSession;
import com.app.questofseoul.domain.entity.ChatTurn;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.enums.ChatRole;
import com.app.questofseoul.domain.enums.ChatSource;
import com.app.questofseoul.domain.enums.ProgressStatus;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.domain.enums.StepNextAction;
import com.app.questofseoul.dto.tour.ChatSessionStatusResponse;
import com.app.questofseoul.dto.tour.ChatTurnsResponse;
import com.app.questofseoul.dto.tour.ProximityResponse;
import com.app.questofseoul.dto.tour.SendMessageResponse;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.exception.ValidationException;
import com.app.questofseoul.repository.ChatSessionRepository;
import com.app.questofseoul.repository.ChatTurnRepository;
import com.app.questofseoul.repository.ScriptLineAssetRepository;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.TourRunRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import com.app.questofseoul.repository.UserMissionAttemptRepository;
import com.app.questofseoul.repository.UserSpotProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private static final int DEFAULT_DELAY_MS = 1500;

    private final ChatSessionRepository chatSessionRepository;
    private final ChatTurnRepository chatTurnRepository;
    private final TourGuideAiService tourGuideAiService;
    private final TourRepository tourRepository;
    private final TourRunRepository tourRunRepository;
    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final UserMissionAttemptRepository userMissionAttemptRepository;
    private final UserSpotProgressRepository userSpotProgressRepository;
    private final ScriptLineAssetRepository scriptLineAssetRepository;

    @Transactional(readOnly = true)
    public ChatTurnsResponse getChatTurns(java.util.UUID userId, Long sessionId) {
        ChatSession session = getAuthorizedSession(userId, sessionId);
        List<ChatTurn> turns = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
        List<ChatTurn> scriptTurns = turns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).toList();
        NextScriptInfo nextScriptInfo = computeNextScriptInfo(session, scriptTurns);

        Map<Long, Integer> scriptTurnIndexMap = new HashMap<>();
        for (int i = 0; i < scriptTurns.size(); i++) {
            scriptTurnIndexMap.put(scriptTurns.get(i).getId(), i);
        }

        List<ChatTurnsResponse.ChatTurnItem> items = new ArrayList<>();
        for (ChatTurn turn : turns) {
            ChatTurnsResponse.ActionDto actionDto = null;
            if (turn.getSource() == ChatSource.SCRIPT) {
                Integer idx = scriptTurnIndexMap.get(turn.getId());
                if (idx != null) {
                    ProximityResponse.ActionDto scriptAction = buildActionForScriptTurn(session, scriptTurns, idx);
                    if (scriptAction != null) {
                        actionDto = new ChatTurnsResponse.ActionDto(scriptAction.type(), scriptAction.nextApi());
                    }
                }
            }

            Integer delayMs = turn.getSource() == ChatSource.SCRIPT ? DEFAULT_DELAY_MS : null;
            items.add(new ChatTurnsResponse.ChatTurnItem(
                    turn.getId(),
                    turn.getRole().name(),
                    turn.getSource().name(),
                    turn.getText(),
                    mapChatTurnAssets(turn),
                    delayMs,
                    actionDto,
                    turn.getCreatedAt() != null ? turn.getCreatedAt().toString() : null
            ));
        }

        String status = nextScriptInfo.hasNextScript ? "ACTIVE" : "COMPLETED";
        return new ChatTurnsResponse(
                session.getId(),
                status,
                nextScriptInfo.nextScriptApi,
                nextScriptInfo.hasNextScript,
                items
        );
    }

    @Transactional
    public ProximityResponse.ChatTurnDto getNextScriptTurn(java.util.UUID userId, Long sessionId, Long nextTurnId) {
        ChatSession session = getAuthorizedSession(userId, sessionId);
        List<ChatTurn> turns = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
        List<ChatTurn> scriptTurns = turns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).toList();
        if (scriptTurns.isEmpty()) {
            throw new ResourceNotFoundException("Script turn not found");
        }

        int requestedIndex = findScriptTurnIndex(scriptTurns, nextTurnId);
        if (requestedIndex < 0) {
            throw new ResourceNotFoundException("Script turn not found");
        }

        int cursor = normalizeCursor(session.getCursorStepIndexSafe(), scriptTurns.size());
        if (requestedIndex >= cursor) {
            session.moveCursorTo(requestedIndex + 1);
        }
        syncScriptOnlySpotCompletion(session, scriptTurns);

        ChatTurn requestedTurn = scriptTurns.get(requestedIndex);
        ProximityResponse.ActionDto action = buildActionForScriptTurn(session, scriptTurns, requestedIndex);

        return new ProximityResponse.ChatTurnDto(
                requestedTurn.getId(),
                requestedTurn.getRole().name(),
                requestedTurn.getSource().name(),
                requestedTurn.getText(),
                mapProximityAssets(requestedTurn),
                DEFAULT_DELAY_MS,
                action
        );
    }

    @Transactional
    public SendMessageResponse sendMessage(java.util.UUID userId, Long sessionId, String text) {
        ChatSession session = getAuthorizedSession(userId, sessionId);

        ChatTurn userTurn = ChatTurn.create(session, ChatSource.USER, ChatRole.USER, text);
        userTurn = chatTurnRepository.save(userTurn);

        String tourContext = buildTourContext(session.getTourRun().getTour().getId());
        List<Map<String, String>> history = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(t -> Map.<String, String>of(
                        "role", t.getRole() == ChatRole.USER ? "user" : "assistant",
                        "content", t.getText() != null ? t.getText() : ""))
                .toList();
        String aiText = tourGuideAiService.generateResponse(tourContext, history);

        ChatTurn llmTurn = ChatTurn.create(session, ChatSource.LLM, ChatRole.GUIDE, aiText);
        llmTurn = chatTurnRepository.save(llmTurn);

        List<ChatTurn> allTurns = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
        List<ChatTurn> scriptTurns = allTurns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).toList();
        NextScriptInfo nextScriptInfo = computeNextScriptInfo(session, scriptTurns);

        return new SendMessageResponse(
                userTurn.getId(),
                text,
                llmTurn.getId(),
                aiText,
                nextScriptInfo.nextScriptApi,
                nextScriptInfo.hasNextScript
        );
    }

    @Transactional
    public ChatSessionStatusResponse getOrCreateSessionStatus(java.util.UUID userId, Long runId, Long spotId) {
        ChatSession session = getOrCreateAuthorizedSession(userId, runId, spotId);
        List<ChatTurn> scriptTurns = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(session.getId()).stream()
                .filter(t -> t.getSource() == ChatSource.SCRIPT)
                .toList();
        syncScriptOnlySpotCompletion(session, scriptTurns);
        int cursor = normalizeCursor(session.getCursorStepIndexSafe(), scriptTurns.size());
        Long lastTurnId = null;
        if (!scriptTurns.isEmpty() && cursor > 0) {
            int lastIndex = Math.min(cursor - 1, scriptTurns.size() - 1);
            lastTurnId = scriptTurns.get(lastIndex).getId();
        }

        boolean completed = Boolean.FALSE.equals(session.getIsActive())
                || (!scriptTurns.isEmpty() && cursor >= scriptTurns.size());
        return new ChatSessionStatusResponse(session.getId(), completed ? "COMPLETED" : "ACTIVE", lastTurnId);
    }

    @Transactional
    public Long getOrCreateSession(java.util.UUID userId, Long runId, Long spotId) {
        return getOrCreateAuthorizedSession(userId, runId, spotId).getId();
    }

    private ChatSession getOrCreateAuthorizedSession(java.util.UUID userId, Long runId, Long spotId) {
        var run = tourRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }
        var spot = tourSpotRepository.findByIdAndTourId(spotId, run.getTour().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Spot not in tour"));
        validateChatSpotType(spot.getType());
        validateSpotUnlocked(runId, spotId);
        return chatSessionRepository.findByTourRunIdAndSpotId(runId, spotId)
                .orElseGet(() -> chatSessionRepository.save(ChatSession.create(run, spot)));
    }

    private ChatSession getAuthorizedSession(java.util.UUID userId, Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        if (!session.getTourRun().getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your chat session");
        }
        validateChatSpotType(session.getSpot().getType());
        validateSpotUnlocked(session.getTourRun().getId(), session.getSpot().getId());
        return session;
    }

    private void validateChatSpotType(SpotType type) {
        if (type != SpotType.MAIN && type != SpotType.SUB) {
            throw new ValidationException("채팅 세션은 MAIN/SUB 스팟에서만 사용할 수 있습니다.");
        }
    }

    private void validateSpotUnlocked(Long runId, Long spotId) {
        var progress = userSpotProgressRepository.findByTourRunIdAndSpotId(runId, spotId).orElse(null);
        if (progress == null || progress.getProgressStatus() == ProgressStatus.PENDING) {
            throw new ValidationException("스팟 Unlock 이후 채팅 세션을 사용할 수 있습니다.");
        }
    }

    private NextScriptInfo computeNextScriptInfo(ChatSession session, List<ChatTurn> scriptTurns) {
        int cursor = normalizeCursor(session.getCursorStepIndexSafe(), scriptTurns.size());
        boolean hasNextScript = cursor < scriptTurns.size();
        String nextScriptApi = hasNextScript
                ? buildNextApi(session.getId(), scriptTurns, cursor)
                : null;
        return new NextScriptInfo(nextScriptApi, hasNextScript);
    }

    private int normalizeCursor(int cursor, int scriptSize) {
        if (cursor < 0) return 0;
        return Math.min(cursor, scriptSize);
    }

    private void syncScriptOnlySpotCompletion(ChatSession session, List<ChatTurn> scriptTurns) {
        if (scriptTurns.isEmpty()) {
            return;
        }

        int cursor = normalizeCursor(session.getCursorStepIndexSafe(), scriptTurns.size());
        if (cursor < scriptTurns.size()) {
            return;
        }

        if (hasMissionSteps(session)) {
            return;
        }

        userSpotProgressRepository.findByTourRunIdAndSpotId(session.getTourRun().getId(), session.getSpot().getId())
                .ifPresent(progress -> {
                    if (progress.getProgressStatus() != ProgressStatus.COMPLETED
                            && progress.getProgressStatus() != ProgressStatus.SKIPPED) {
                        progress.complete();
                    }
                });
    }

    private boolean hasMissionSteps(ChatSession session) {
        String language = (session.getLanguage() != null && !session.getLanguage().isBlank())
                ? session.getLanguage()
                : "ko";
        List<SpotContentStep> missionSteps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(session.getSpot().getId(), StepKind.MISSION, language);
        if (missionSteps.isEmpty() && !"ko".equals(language)) {
            missionSteps = spotContentStepRepository
                    .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(session.getSpot().getId(), StepKind.MISSION, "ko");
        }
        return !missionSteps.isEmpty();
    }

    private int findScriptTurnIndex(List<ChatTurn> scriptTurns, Long turnId) {
        for (int i = 0; i < scriptTurns.size(); i++) {
            if (scriptTurns.get(i).getId().equals(turnId)) {
                return i;
            }
        }
        return -1;
    }

    private String buildNextApi(Long sessionId, List<ChatTurn> scriptTurns, int nextIndex) {
        if (nextIndex < 0 || nextIndex >= scriptTurns.size()) return null;
        return "/api/v1/chat-sessions/" + sessionId + "/turns/" + scriptTurns.get(nextIndex).getId();
    }

    private List<ChatTurnsResponse.AssetDto> mapChatTurnAssets(ChatTurn turn) {
        if (turn.getSource() != ChatSource.SCRIPT || turn.getScriptLine() == null) {
            return List.of();
        }
        return scriptLineAssetRepository.findByScriptLine_IdOrderBySortOrderAsc(turn.getScriptLine().getId()).stream()
                .map(a -> new ChatTurnsResponse.AssetDto(
                        a.getAsset().getId(),
                        a.getAsset().getAssetType() != null ? a.getAsset().getAssetType().name() : "IMAGE",
                        a.getAsset().getUrl(),
                        a.getAsset().getMetadataJson()))
                .toList();
    }

    private List<ProximityResponse.AssetDto> mapProximityAssets(ChatTurn turn) {
        if (turn.getSource() != ChatSource.SCRIPT || turn.getScriptLine() == null) {
            return List.of();
        }
        return scriptLineAssetRepository.findByScriptLine_IdOrderBySortOrderAsc(turn.getScriptLine().getId()).stream()
                .map(a -> new ProximityResponse.AssetDto(
                        a.getAsset().getId(),
                        a.getAsset().getAssetType() != null ? a.getAsset().getAssetType().name() : "IMAGE",
                        a.getAsset().getUrl(),
                        a.getAsset().getMetadataJson()))
                .toList();
    }

    private ProximityResponse.ActionDto buildActionForScriptTurn(
            ChatSession session,
            List<ChatTurn> scriptTurns,
            int currentIndex
    ) {
        String nextApi = buildNextApi(session.getId(), scriptTurns, currentIndex + 1);
        if (!isStepBoundary(scriptTurns, currentIndex)) {
            return new ProximityResponse.ActionDto("AUTO_NEXT", nextApi, null, null);
        }

        ChatTurn turn = scriptTurns.get(currentIndex);
        StepNextAction nextAction = turn.getStep() != null ? turn.getStep().getNextAction() : null;
        if (nextAction == StepNextAction.MISSION_CHOICE) {
            Long preferredMissionId = turn.getStep() != null && turn.getStep().getMission() != null
                    ? turn.getStep().getMission().getId()
                    : null;
            Long missionStepId = resolveNextMissionStepId(
                    session.getTourRun().getId(),
                    session.getSpot().getId(),
                    session.getLanguage(),
                    preferredMissionId
            );
            if (missionStepId != null) {
                return new ProximityResponse.ActionDto("MISSION_CHOICE", nextApi, "게임 시작", missionStepId);
            }
        }

        if (nextApi != null) {
            return new ProximityResponse.ActionDto("NEXT", nextApi, "다음", null);
        }
        return new ProximityResponse.ActionDto("NEXT", null, "다음", session.getSpot().getId());
    }

    private Long resolveNextMissionStepId(Long runId, Long spotId, String language, Long preferredMissionId) {
        String lang = language != null && !language.isBlank() ? language : "ko";
        List<SpotContentStep> missionSteps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.MISSION, lang);
        if (missionSteps.isEmpty() && !"ko".equals(lang)) {
            missionSteps = spotContentStepRepository
                    .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.MISSION, "ko");
        }
        if (missionSteps.isEmpty()) {
            return null;
        }

        if (preferredMissionId != null) {
            for (SpotContentStep missionStep : missionSteps) {
                if (missionStep.getMission() != null
                        && preferredMissionId.equals(missionStep.getMission().getId())) {
                    return missionStep.getId();
                }
            }
        }

        Set<Long> attemptedStepIds = new HashSet<>();
        userMissionAttemptRepository.findByTourRun_IdAndStep_Spot_IdOrderByAttemptNoAsc(runId, spotId)
                .forEach(attempt -> attemptedStepIds.add(attempt.getStep().getId()));

        for (SpotContentStep missionStep : missionSteps) {
            if (!attemptedStepIds.contains(missionStep.getId())) {
                return missionStep.getId();
            }
        }
        return missionSteps.get(missionSteps.size() - 1).getId();
    }

    private boolean isStepBoundary(List<ChatTurn> scriptTurns, int currentIndex) {
        if (currentIndex >= scriptTurns.size() - 1) {
            return true;
        }
        ChatTurn currentTurn = scriptTurns.get(currentIndex);
        ChatTurn nextTurn = scriptTurns.get(currentIndex + 1);
        Long currentStepId = currentTurn.getStep() != null ? currentTurn.getStep().getId() : null;
        Long nextStepId = nextTurn.getStep() != null ? nextTurn.getStep().getId() : null;
        return !Objects.equals(currentStepId, nextStepId);
    }

    private record NextScriptInfo(String nextScriptApi, Boolean hasNextScript) {}

    private String buildTourContext(Long tourId) {
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

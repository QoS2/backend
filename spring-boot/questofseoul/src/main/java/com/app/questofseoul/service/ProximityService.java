package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.*;
import com.app.questofseoul.dto.tour.ProximityResponse;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProximityService {

    private static final double EARTH_RADIUS_M = 6_371_000;
    private static final int DEFAULT_DELAY_MS = 1500;

    private final TourRunRepository tourRunRepository;
    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final SpotScriptLineRepository spotScriptLineRepository;
    private final ScriptLineAssetRepository scriptLineAssetRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatTurnRepository chatTurnRepository;
    private final UserMissionAttemptRepository userMissionAttemptRepository;
    private final UserSpotProgressRepository userSpotProgressRepository;
    private final UserTreasureStatusRepository userTreasureStatusRepository;

    @Transactional
    public ProximityResponse checkProximity(java.util.UUID userId, Long runId, BigDecimal lat, BigDecimal lng, String lang) {
        TourRun run = tourRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }

        Long tourId = run.getTour().getId();
        String language = (lang != null && !lang.isBlank()) ? lang : "ko";
        double latD = lat.doubleValue();
        double lngD = lng.doubleValue();

        List<TourSpot> spots = tourSpotRepository.findByTourIdOrderByOrderIndexAsc(tourId);
        Map<Long, ProgressStatus> progressStatusBySpotId = new HashMap<>();
        for (UserSpotProgress progress : userSpotProgressRepository.findByTourRunId(runId)) {
            if (progress.getSpot() != null && progress.getSpot().getId() != null) {
                progressStatusBySpotId.put(progress.getSpot().getId(), progress.getProgressStatus());
            }
        }
        Long nextRouteSpotId = resolveNextRouteSpotId(spots, progressStatusBySpotId);

        // 1순위: MAIN/SUB + GUIDE 스텝 → Place Unlock + 가이드 반환
        List<SpotDistanceCandidate> guideCandidates = new ArrayList<>();
        for (TourSpot spot : spots) {
            if (spot.getType() != SpotType.MAIN && spot.getType() != SpotType.SUB) continue;
            if (spot.getLatitude() == null || spot.getLongitude() == null) continue;

            double distanceM = haversineM(latD, lngD, spot.getLatitude(), spot.getLongitude());
            int radiusM = spot.getRadiusM() != null ? spot.getRadiusM() : 50;
            if (distanceM > radiusM) continue;
            guideCandidates.add(new SpotDistanceCandidate(spot, distanceM));
        }

        guideCandidates.sort(
                Comparator.comparingInt((SpotDistanceCandidate c) ->
                                guideCandidatePriority(c.spot(), nextRouteSpotId, progressStatusBySpotId))
                        .thenComparingDouble(SpotDistanceCandidate::distanceM)
                        .thenComparing(c -> c.spot().getOrderIndex(), Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(c -> c.spot().getId(), Comparator.nullsLast(Long::compareTo))
        );

        for (SpotDistanceCandidate candidate : guideCandidates) {
            TourSpot spot = candidate.spot();

            ensureAndUnlockSpotProgress(run, spot);

            List<SpotContentStep> guideSteps = spotContentStepRepository.findBySpotIdAndLanguageOrderByStepIndexAsc(spot.getId(), language)
                    .stream().filter(s -> s.getKind() == StepKind.GUIDE).toList();
            if (guideSteps.isEmpty()) continue;

            ChatSession session = chatSessionRepository.findByTourRunIdAndSpotId(runId, spot.getId())
                    .orElseGet(() -> chatSessionRepository.save(ChatSession.create(run, spot)));

            List<ChatTurn> existingTurns = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(session.getId());
            long scriptCount = existingTurns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).count();
            if (scriptCount > 0) {
                ProximityResponse existingResponse = buildProximityResponseFromExisting(session, spot, existingTurns, language);
                if (existingResponse != null) {
                    return existingResponse;
                }
                continue;
            }

            // 모든 스크립트라인을 ChatTurn으로 저장
            List<ChatTurn> savedTurns = new ArrayList<>();
            for (SpotContentStep step : guideSteps) {
                List<SpotScriptLine> lines = spotScriptLineRepository.findByStep_IdOrderBySeqAsc(step.getId());
                for (SpotScriptLine line : lines) {
                    savedTurns.add(chatTurnRepository.save(ChatTurn.createScript(session, step, line)));
                }
            }

            if (savedTurns.isEmpty()) continue;

            // 첫 번째 턴 반환 후 커서를 다음 턴으로 이동
            ChatTurn firstTurn = savedTurns.get(0);
            session.moveCursorTo(1);
            syncScriptOnlySpotCompletion(session, savedTurns, language);
            List<ProximityResponse.AssetDto> firstAssets = getAssetsForTurn(firstTurn);
            ProximityResponse.ActionDto action = resolveActionForTurn(
                    run,
                    spot,
                    session.getId(),
                    savedTurns,
                    0,
                    language
            );

            ProximityResponse.ChatTurnDto message = new ProximityResponse.ChatTurnDto(
                    firstTurn.getId(), "GUIDE", "SCRIPT", firstTurn.getText(),
                    firstAssets, DEFAULT_DELAY_MS, action);

            return new ProximityResponse("PROXIMITY", "GUIDE", session.getId(),
                    new ProximityResponse.ProximityContext("SPOT", spot.getId(), spot.getTitle(), spot.getType().name()),
                    message);
        }

        // 2순위: TREASURE 근접 → 첫 발견 시 Unlock + 알람
        for (TourSpot spot : spots) {
            if (spot.getType() != SpotType.TREASURE) continue;
            if (spot.getLatitude() == null || spot.getLongitude() == null) continue;
            if (haversineM(latD, lngD, spot.getLatitude(), spot.getLongitude()) > (spot.getRadiusM() != null ? spot.getRadiusM() : 50)) continue;

            UserTreasureStatus status = ensureAndUnlockTreasureStatus(run, spot);
            if (status != null) {
                return ProximityResponse.treasureFound(spot.getId(), spot.getTitle());
            }
        }

        // 3순위: PHOTO Spot 근접 → 알람
        for (TourSpot spot : spots) {
            if (spot.getType() != SpotType.PHOTO) continue;
            if (spot.getLatitude() == null || spot.getLongitude() == null) continue;
            if (haversineM(latD, lngD, spot.getLatitude(), spot.getLongitude()) > (spot.getRadiusM() != null ? spot.getRadiusM() : 50)) continue;

            return ProximityResponse.photoSpotFound(spot.getId(), spot.getTitle());
        }

        return null;
    }

    private void ensureAndUnlockSpotProgress(TourRun run, TourSpot spot) {
        UserSpotProgress progress = userSpotProgressRepository.findByTourRunIdAndSpotId(run.getId(), spot.getId())
                .orElseGet(() -> userSpotProgressRepository.save(UserSpotProgress.create(run, spot)));
        progress.unlock();
    }

    /** LOCKED → UNLOCKED 된 경우에만 반환 (새로 발견한 보물) */
    private UserTreasureStatus ensureAndUnlockTreasureStatus(TourRun run, TourSpot spot) {
        UserTreasureStatus status = userTreasureStatusRepository.findByTourRunIdAndTreasureSpotId(run.getId(), spot.getId())
                .orElseGet(() -> userTreasureStatusRepository.save(UserTreasureStatus.create(run, spot)));
        if (status.getStatus() == TreasureStatus.LOCKED) {
            status.unlock();
            return status;
        }
        return null;
    }

    private List<ProximityResponse.AssetDto> getAssetsForTurn(ChatTurn turn) {
        if (turn == null || turn.getScriptLine() == null) return List.of();
        return scriptLineAssetRepository.findByScriptLine_IdOrderBySortOrderAsc(turn.getScriptLine().getId()).stream()
                .map(a -> new ProximityResponse.AssetDto(
                        a.getAsset().getId(),
                        a.getAsset().getAssetType() != null ? a.getAsset().getAssetType().name() : "IMAGE",
                        a.getAsset().getUrl(),
                        a.getAsset().getMetadataJson()))
                .toList();
    }

    private ProximityResponse buildProximityResponseFromExisting(ChatSession session, TourSpot spot,
                                                                  List<ChatTurn> turns, String language) {
        List<ChatTurn> scriptTurns = turns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).toList();
        if (scriptTurns.isEmpty()) return null;

        int cursor = Math.max(0, Math.min(session.getCursorStepIndexSafe(), scriptTurns.size()));
        int currentIndex;
        if (cursor <= 0) {
            currentIndex = 0;
            session.moveCursorTo(Math.min(1, scriptTurns.size()));
        } else if (cursor >= scriptTurns.size()) {
            currentIndex = scriptTurns.size() - 1;
        } else {
            currentIndex = cursor - 1;
        }
        syncScriptOnlySpotCompletion(session, scriptTurns, language);

        ChatTurn turn = scriptTurns.get(currentIndex);
        ProximityResponse.ActionDto action = resolveActionForTurn(
                session.getTourRun(),
                spot,
                session.getId(),
                scriptTurns,
                currentIndex,
                language
        );

        ProximityResponse.ChatTurnDto message = new ProximityResponse.ChatTurnDto(
                turn.getId(), turn.getRole().name(), turn.getSource().name(), turn.getText(),
                getAssetsForTurn(turn), DEFAULT_DELAY_MS, action);

        return new ProximityResponse("PROXIMITY", "GUIDE", session.getId(),
                new ProximityResponse.ProximityContext("SPOT", spot.getId(), spot.getTitle(), spot.getType().name()),
                message);
    }

    private void syncScriptOnlySpotCompletion(ChatSession session, List<ChatTurn> scriptTurns, String language) {
        if (scriptTurns.isEmpty()) {
            return;
        }

        int cursor = Math.max(0, Math.min(session.getCursorStepIndexSafe(), scriptTurns.size()));
        if (cursor < scriptTurns.size()) {
            return;
        }

        if (hasMissionSteps(session, language)) {
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

    private boolean hasMissionSteps(ChatSession session, String language) {
        String lang = (language != null && !language.isBlank())
                ? language
                : ((session.getLanguage() != null && !session.getLanguage().isBlank()) ? session.getLanguage() : "ko");

        List<SpotContentStep> missionSteps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(session.getSpot().getId(), StepKind.MISSION, lang);
        if (missionSteps.isEmpty() && !"ko".equals(lang)) {
            missionSteps = spotContentStepRepository
                    .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(session.getSpot().getId(), StepKind.MISSION, "ko");
        }
        return !missionSteps.isEmpty();
    }

    private ProximityResponse.ActionDto resolveActionForTurn(
            TourRun run,
            TourSpot spot,
            Long sessionId,
            List<ChatTurn> scriptTurns,
            int currentIndex,
            String language
    ) {
        String nextApi = buildNextApi(sessionId, scriptTurns, currentIndex + 1);
        if (!isStepBoundary(scriptTurns, currentIndex)) {
            return new ProximityResponse.ActionDto("AUTO_NEXT", nextApi, null, null);
        }

        ChatTurn turn = scriptTurns.get(currentIndex);
        StepNextAction nextAction = turn.getStep() != null ? turn.getStep().getNextAction() : null;
        if (nextAction == StepNextAction.MISSION_CHOICE) {
            Long preferredMissionId = turn.getStep() != null && turn.getStep().getMission() != null
                    ? turn.getStep().getMission().getId()
                    : null;
            Long missionStepId = resolveNextMissionStepId(run.getId(), spot.getId(), language, preferredMissionId);
            if (missionStepId != null) {
                return new ProximityResponse.ActionDto("MISSION_CHOICE", nextApi, "게임 시작", missionStepId);
            }
        }

        if (nextApi != null) {
            return new ProximityResponse.ActionDto("NEXT", nextApi, "다음", null);
        }
        return new ProximityResponse.ActionDto("NEXT", null, "다음", spot.getId());
    }

    private Long resolveNextMissionStepId(Long runId, Long spotId, String language, Long preferredMissionId) {
        String lang = (language != null && !language.isBlank()) ? language : "ko";
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
        for (UserMissionAttempt attempt : userMissionAttemptRepository
                .findByTourRun_IdAndStep_Spot_IdOrderByAttemptNoAsc(runId, spotId)) {
            attemptedStepIds.add(attempt.getStep().getId());
        }

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

    private String buildNextApi(Long sessionId, List<ChatTurn> scriptTurns, int nextIndex) {
        if (nextIndex < 0 || nextIndex >= scriptTurns.size()) return null;
        return "/api/v1/chat-sessions/" + sessionId + "/turns/" + scriptTurns.get(nextIndex).getId();
    }

    private Long resolveNextRouteSpotId(List<TourSpot> spots, Map<Long, ProgressStatus> progressStatusBySpotId) {
        for (TourSpot spot : spots) {
            if (spot.getType() != SpotType.MAIN && spot.getType() != SpotType.SUB) {
                continue;
            }
            ProgressStatus status = progressStatusBySpotId.get(spot.getId());
            if (status != ProgressStatus.COMPLETED && status != ProgressStatus.SKIPPED) {
                return spot.getId();
            }
        }
        return null;
    }

    private int guideCandidatePriority(
            TourSpot spot,
            Long nextRouteSpotId,
            Map<Long, ProgressStatus> progressStatusBySpotId
    ) {
        if (nextRouteSpotId != null && nextRouteSpotId.equals(spot.getId())) {
            return 0;
        }
        ProgressStatus status = progressStatusBySpotId.get(spot.getId());
        if (status == ProgressStatus.COMPLETED || status == ProgressStatus.SKIPPED) {
            return 2;
        }
        return 1;
    }

    private double haversineM(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    private record SpotDistanceCandidate(TourSpot spot, double distanceM) {}
}

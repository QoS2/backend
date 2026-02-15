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
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProximityService {

    private static final double EARTH_RADIUS_M = 6_371_000;

    private final TourRunRepository tourRunRepository;
    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final SpotScriptLineRepository spotScriptLineRepository;
    private final ScriptLineAssetRepository scriptLineAssetRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatTurnRepository chatTurnRepository;
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

        // 1순위: MAIN/SUB + GUIDE 스텝 → Place Unlock + 가이드 반환
        for (TourSpot spot : spots) {
            if (spot.getType() != SpotType.MAIN && spot.getType() != SpotType.SUB) continue;
            if (spot.getLatitude() == null || spot.getLongitude() == null) continue;
            if (haversineM(latD, lngD, spot.getLatitude(), spot.getLongitude()) > (spot.getRadiusM() != null ? spot.getRadiusM() : 50)) continue;

            ensureAndUnlockSpotProgress(run, spot);

            List<SpotContentStep> guideSteps = spotContentStepRepository.findBySpotIdAndLanguageOrderByStepIndexAsc(spot.getId(), language)
                    .stream().filter(s -> s.getKind() == StepKind.GUIDE).toList();
            if (guideSteps.isEmpty()) continue;

            ChatSession session = chatSessionRepository.findByTourRunIdAndSpotId(runId, spot.getId())
                    .orElseGet(() -> chatSessionRepository.save(ChatSession.create(run, spot)));

            List<ChatTurn> existingTurns = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(session.getId());
            long scriptCount = existingTurns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).count();
            if (scriptCount > 0) {
                return buildProximityResponse(session, spot, existingTurns, "SPOT", spot.getId(), language);
            }

            int totalLines = guideSteps.stream()
                    .mapToInt(s -> spotScriptLineRepository.findByStep_IdOrderBySeqAsc(s.getId()).size())
                    .sum();
            List<ProximityResponse.ChatTurnDto> turnDtos = new ArrayList<>();
            int lineIdx = 0;
            SpotContentStep lastGuideStep = guideSteps.get(guideSteps.size() - 1);
            for (SpotContentStep step : guideSteps) {
                List<SpotScriptLine> lines = spotScriptLineRepository.findByStep_IdOrderBySeqAsc(step.getId());
                for (SpotScriptLine line : lines) {
                    lineIdx++;
                    boolean isLastLine = (lineIdx == totalLines);
                    ProximityResponse.ActionDto action = resolveAction(spot, lastGuideStep, isLastLine);
                    List<ProximityResponse.AssetDto> assets = scriptLineAssetRepository
                            .findByScriptLine_IdOrderBySortOrderAsc(line.getId()).stream()
                            .map(a -> new ProximityResponse.AssetDto(
                                    a.getAsset().getId(), "IMAGE", a.getAsset().getUrl(), a.getAsset().getMetadataJson()))
                            .toList();
                    ChatTurn turn = ChatTurn.create(session, ChatSource.SCRIPT, ChatRole.GUIDE, line.getText());
                    turn = chatTurnRepository.save(turn);
                    turnDtos.add(new ProximityResponse.ChatTurnDto(
                            turn.getId(), "GUIDE", "SCRIPT", line.getText(), assets, action));
                }
            }
            return new ProximityResponse("PROXIMITY", "GUIDE", session.getId(),
                    new ProximityResponse.ProximityContext("SPOT", spot.getId(), spot.getTitle()), turnDtos);
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

    private ProximityResponse.ActionDto resolveAction(TourSpot spot,
                                                     SpotContentStep lastGuideStep, boolean isLastLine) {
        if (!isLastLine || lastGuideStep == null) {
            return new ProximityResponse.ActionDto("NEXT", "다음", spot.getId());
        }
        StepNextAction nextAction = lastGuideStep.getNextAction();
        if (nextAction == StepNextAction.MISSION_CHOICE) {
            List<SpotContentStep> missionSteps = spotContentStepRepository
                    .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spot.getId(), StepKind.MISSION, "ko");
            Long missionStepId = missionSteps.isEmpty() ? spot.getId() : missionSteps.get(0).getId();
            return new ProximityResponse.ActionDto("MISSION_CHOICE", "게임 시작", missionStepId);
        }
        return new ProximityResponse.ActionDto("NEXT", "다음", spot.getId());
    }

    private ProximityResponse buildProximityResponse(ChatSession session, TourSpot spot,
                                                     List<ChatTurn> turns, String refType, Long refId, String language) {
        List<SpotContentStep> guideSteps = spotContentStepRepository
                .findBySpotIdAndLanguageOrderByStepIndexAsc(spot.getId(), language)
                .stream().filter(s -> s.getKind() == StepKind.GUIDE).toList();
        SpotContentStep lastGuideStep = guideSteps.isEmpty() ? null : guideSteps.get(guideSteps.size() - 1);
        List<ChatTurn> scriptTurns = turns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).toList();
        int totalScript = scriptTurns.size();

        List<ProximityResponse.ChatTurnDto> dtos = new ArrayList<>();
        for (int i = 0; i < scriptTurns.size(); i++) {
            ChatTurn t = scriptTurns.get(i);
            boolean isLast = (i == totalScript - 1);
            ProximityResponse.ActionDto action = isLast && lastGuideStep != null
                    ? resolveAction(spot, lastGuideStep, true)
                    : new ProximityResponse.ActionDto("NEXT", "다음", spot.getId());
            dtos.add(new ProximityResponse.ChatTurnDto(
                    t.getId(), t.getRole().name(), t.getSource().name(), t.getText(),
                    List.of(), action));
        }
        return new ProximityResponse("PROXIMITY", "GUIDE", session.getId(),
                new ProximityResponse.ProximityContext(refType, refId, spot.getTitle()), dtos);
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
}

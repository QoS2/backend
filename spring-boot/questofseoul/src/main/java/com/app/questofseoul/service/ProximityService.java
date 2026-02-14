package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.ChatRole;
import com.app.questofseoul.domain.enums.ChatSource;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.domain.enums.StepNextAction;
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

    private final TourRunRepository tourRunRepository;
    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final SpotScriptLineRepository spotScriptLineRepository;
    private final ScriptLineAssetRepository scriptLineAssetRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatTurnRepository chatTurnRepository;

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
        for (TourSpot spot : spots) {
            if (spot.getLatitude() == null || spot.getLongitude() == null) continue;
            double dist = haversineM(latD, lngD, spot.getLatitude(), spot.getLongitude());
            int radius = spot.getRadiusM() != null ? spot.getRadiusM() : 50;
            if (dist > radius) continue;

            // GUIDE 스텝이 있는지 확인
            List<SpotContentStep> guideSteps = spotContentStepRepository.findBySpotIdAndLanguageOrderByStepIndexAsc(spot.getId(), language)
                    .stream().filter(s -> s.getKind() == StepKind.GUIDE).toList();
            if (guideSteps.isEmpty()) continue;

            ChatSession session = chatSessionRepository.findByTourRunIdAndSpotId(runId, spot.getId())
                    .orElseGet(() -> chatSessionRepository.save(ChatSession.create(run, spot)));

            // 이미 스크립트 턴이 있으면 재사용
            List<ChatTurn> existingTurns = chatTurnRepository.findBySession_IdOrderByCreatedAtAsc(session.getId());
            long scriptCount = existingTurns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).count();
            if (scriptCount > 0) {
                return buildProximityResponse(session, spot, existingTurns, "SPOT", spot.getId(), language);
            }

            // 스크립트 턴 생성 및 저장
            int totalLines = guideSteps.stream()
                    .mapToInt(s -> spotScriptLineRepository.findByStep_IdOrderBySeqAsc(s.getId()).size())
                    .sum();
            List<ProximityResponse.ChatTurnDto> turnDtos = new ArrayList<>();
            int lineIdx = 0;
            SpotContentStep lastGuideStep = guideSteps.isEmpty() ? null : guideSteps.get(guideSteps.size() - 1);
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

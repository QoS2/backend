package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.MediaAsset;
import com.app.questofseoul.domain.entity.Mission;
import com.app.questofseoul.domain.entity.ScriptLineAsset;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.SpotScriptLine;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.AssetType;
import com.app.questofseoul.domain.enums.LineAssetUsage;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.domain.enums.StepNextAction;
import com.app.questofseoul.dto.admin.*;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.ChatTurnAssetRepository;
import com.app.questofseoul.repository.MediaAssetRepository;
import com.app.questofseoul.repository.ScriptLineAssetRepository;
import com.app.questofseoul.repository.SpotAssetRepository;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.SpotScriptLineRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGuideService {

    private final EntityManager entityManager;
    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final SpotScriptLineRepository spotScriptLineRepository;
    private final ScriptLineAssetRepository scriptLineAssetRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final SpotAssetRepository spotAssetRepository;
    private final ChatTurnAssetRepository chatTurnAssetRepository;

    @Transactional(readOnly = true)
    public GuideStepsAdminResponse getGuideSteps(Long tourId, Long spotId) {
        return getGuideSteps(tourId, spotId, "ko");
    }

    @Transactional(readOnly = true)
    public GuideStepsAdminResponse getGuideSteps(Long tourId, Long spotId, String language) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        String lang = normalizeLanguage(language);

        List<SpotContentStep> guideSteps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.GUIDE, lang);
        List<SpotContentStep> missionSteps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.MISSION, lang);
        if (missionSteps.isEmpty() && !"ko".equals(lang)) {
            missionSteps = spotContentStepRepository
                    .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.MISSION, "ko");
        }
        Map<Long, Long> missionIdToStepId = missionSteps.stream()
                .filter(s -> s.getMission() != null && s.getMission().getId() != null)
                .collect(Collectors.toMap(
                        s -> s.getMission().getId(),
                        SpotContentStep::getId,
                        (first, ignored) -> first
                ));

        List<GuideStepAdminResponse> stepResponses = new ArrayList<>();
        for (SpotContentStep step : guideSteps) {
            String nextAction = step.getNextAction() != null ? step.getNextAction().name() : null;
            Long missionStepId = step.getMission() != null ? missionIdToStepId.get(step.getMission().getId()) : null;
            List<SpotScriptLine> lines = spotScriptLineRepository.findByStep_IdOrderBySeqAsc(step.getId());
            List<GuideLineResponse> lineResponses = new ArrayList<>();
            int seq = 1;
            for (SpotScriptLine line : lines) {
                List<ScriptLineAsset> assets = scriptLineAssetRepository.findByScriptLine_IdOrderBySortOrderAsc(line.getId());
                List<GuideAssetResponse> assetResponses = assets.stream()
                        .map(a -> new GuideAssetResponse(
                                a.getAsset().getId(),
                                a.getAsset().getUrl(),
                                a.getAsset().getAssetType().name(),
                                a.getUsage().name()))
                        .toList();
                lineResponses.add(new GuideLineResponse(line.getId(), seq++, line.getText(), assetResponses));
            }
            stepResponses.add(new GuideStepAdminResponse(
                    step.getId(),
                    step.getStepIndex(),
                    step.getTitle(),
                    nextAction,
                    missionStepId,
                    lineResponses));
        }

        return new GuideStepsAdminResponse(lang, stepResponses);
    }

    @Transactional
    public GuideStepsAdminResponse saveGuideSteps(Long tourId, Long spotId, GuideStepsSaveRequest req) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));

        String lang = normalizeLanguage(req.language());
        List<SpotContentStep> allLangSteps = spotContentStepRepository
                .findBySpot_IdAndLanguageOrderByStepIndexAsc(spotId, lang);
        List<SpotContentStep> existing = allLangSteps.stream()
                .filter(s -> s.getKind() == StepKind.GUIDE)
                .toList();
        Set<Integer> occupiedStepIndexes = allLangSteps.stream()
                .filter(s -> s.getKind() != StepKind.GUIDE)
                .map(SpotContentStep::getStepIndex)
                .collect(Collectors.toCollection(HashSet::new));

        Set<Long> assetIdsToCheck = existing.stream()
                .flatMap(s -> spotScriptLineRepository.findByStep_IdOrderBySeqAsc(s.getId()).stream())
                .flatMap(l -> scriptLineAssetRepository.findByScriptLine_IdOrderBySortOrderAsc(l.getId()).stream())
                .map(sla -> sla.getAsset().getId())
                .collect(Collectors.toSet());

        spotContentStepRepository.deleteAll(existing);
        entityManager.flush();

        for (GuideStepSaveRequest stepReq : req.steps()) {
            int stepIndex = nextAvailableStepIndex(occupiedStepIndexes);
            SpotContentStep step = SpotContentStep.create(spot, StepKind.GUIDE, stepIndex);
            step.setLanguage(lang);
            step.setTitle(stepReq.stepTitle() != null && !stepReq.stepTitle().isBlank()
                    ? stepReq.stepTitle()
                    : spot.getTitle());
            StepNextAction nextAction = parseNextAction(stepReq.nextAction());
            step.setNextAction(nextAction);
            if (nextAction == StepNextAction.MISSION_CHOICE) {
                step.setMission(resolveLinkedMission(spotId, stepReq.missionStepId()));
            } else {
                step.setMission(null);
            }
            step = spotContentStepRepository.save(step);
            occupiedStepIndexes.add(stepIndex);

            int seq = 1;
            for (GuideLineRequest lineReq : stepReq.lines()) {
                SpotScriptLine line = SpotScriptLine.create(step, seq++, lineReq.text());
                line = spotScriptLineRepository.save(line);

                int sortOrder = 1;
                for (GuideAssetRequest assetReq : lineReq.assets()) {
                    AssetType assetType = AssetType.valueOf(assetReq.assetType());
                    LineAssetUsage usage = LineAssetUsage.valueOf(assetReq.usage());
                    String mimeType = inferMimeType(assetType, assetReq.url());
                    MediaAsset asset = MediaAsset.create(assetType, assetReq.url(), mimeType);
                    asset = mediaAssetRepository.save(asset);
                    ScriptLineAsset sla = ScriptLineAsset.create(line, asset, usage);
                    sla.setSortOrder(sortOrder++);
                    scriptLineAssetRepository.save(sla);
                }
            }
        }

        deleteOrphanedMediaAssets(assetIdsToCheck);
        return getGuideSteps(tourId, spotId, lang);
    }

    private Mission resolveLinkedMission(Long spotId, Long missionStepId) {
        if (missionStepId == null) {
            return null;
        }
        SpotContentStep linkedMissionStep = spotContentStepRepository.findById(missionStepId)
                .orElseThrow(() -> new ResourceNotFoundException("Linked mission step not found"));
        if (!linkedMissionStep.getSpot().getId().equals(spotId) || linkedMissionStep.getKind() != StepKind.MISSION) {
            throw new IllegalArgumentException("Linked mission step must belong to the same spot and be MISSION kind");
        }
        if (linkedMissionStep.getMission() == null) {
            throw new IllegalArgumentException("Linked mission step has no mission");
        }
        return linkedMissionStep.getMission();
    }

    private static int nextAvailableStepIndex(Set<Integer> occupiedStepIndexes) {
        int candidate = 0;
        while (occupiedStepIndexes.contains(candidate)) {
            candidate++;
        }
        return candidate;
    }

    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "ko";
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private static StepNextAction parseNextAction(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return StepNextAction.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String inferMimeType(AssetType type, String url) {
        if (url == null) return "application/octet-stream";
        String lower = url.toLowerCase();
        if (type == AssetType.IMAGE) {
            if (lower.contains(".webp")) return "image/webp";
            if (lower.contains(".png")) return "image/png";
            if (lower.contains(".gif")) return "image/gif";
            return "image/jpeg";
        }
        if (type == AssetType.AUDIO) {
            if (lower.contains(".mp3")) return "audio/mpeg";
            if (lower.contains(".wav")) return "audio/wav";
            if (lower.contains(".ogg")) return "audio/ogg";
            if (lower.contains(".m4a")) return "audio/m4a";
            return "audio/mpeg";
        }
        return "application/octet-stream";
    }

    private void deleteOrphanedMediaAssets(Set<Long> assetIds) {
        for (Long assetId : assetIds) {
            if (spotAssetRepository.existsByAsset_Id(assetId) || chatTurnAssetRepository.existsByAsset_Id(assetId)) {
                continue;
            }
            mediaAssetRepository.findById(assetId).ifPresent(mediaAssetRepository::delete);
            log.debug("Deleted orphaned MediaAsset: {}", assetId);
        }
    }
}

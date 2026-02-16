package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.MediaAsset;
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
import java.util.List;
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
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));

        List<SpotContentStep> guideSteps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.GUIDE, "ko");

        List<GuideStepAdminResponse> stepResponses = new ArrayList<>();
        for (int i = 0; i < guideSteps.size(); i++) {
            SpotContentStep step = guideSteps.get(i);
            String nextAction = step.getNextAction() != null ? step.getNextAction().name() : null;
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
                    lineResponses));
        }

        return new GuideStepsAdminResponse("ko", stepResponses);
    }

    @Transactional
    public GuideStepsAdminResponse saveGuideSteps(Long tourId, Long spotId, GuideStepsSaveRequest req) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));

        String lang = req.language() != null && !req.language().isBlank() ? req.language() : "ko";
        List<SpotContentStep> existing = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.GUIDE, lang);

        Set<Long> assetIdsToCheck = existing.stream()
                .flatMap(s -> spotScriptLineRepository.findByStep_IdOrderBySeqAsc(s.getId()).stream())
                .flatMap(l -> scriptLineAssetRepository.findByScriptLine_IdOrderBySortOrderAsc(l.getId()).stream())
                .map(sla -> sla.getAsset().getId())
                .collect(Collectors.toSet());

        spotContentStepRepository.deleteAll(existing);
        entityManager.flush();

        for (int stepIndex = 0; stepIndex < req.steps().size(); stepIndex++) {
            GuideStepSaveRequest stepReq = req.steps().get(stepIndex);
            SpotContentStep step = SpotContentStep.create(spot, StepKind.GUIDE, stepIndex);
            step.setTitle(stepReq.stepTitle() != null && !stepReq.stepTitle().isBlank()
                    ? stepReq.stepTitle()
                    : spot.getTitle());
            step.setNextAction(parseNextAction(stepReq.nextAction()));
            step = spotContentStepRepository.save(step);

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
        return getGuideSteps(tourId, spotId);
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

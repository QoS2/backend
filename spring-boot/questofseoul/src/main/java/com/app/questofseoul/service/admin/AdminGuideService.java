package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.MediaAsset;
import com.app.questofseoul.domain.entity.ScriptLineAsset;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.SpotScriptLine;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.AssetType;
import com.app.questofseoul.domain.enums.LineAssetUsage;
import com.app.questofseoul.domain.enums.StepKind;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGuideService {

    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final SpotScriptLineRepository spotScriptLineRepository;
    private final ScriptLineAssetRepository scriptLineAssetRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final SpotAssetRepository spotAssetRepository;
    private final ChatTurnAssetRepository chatTurnAssetRepository;

    @Transactional(readOnly = true)
    public GuideAdminResponse getGuide(Long tourId, Long spotId) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));

        List<SpotContentStep> guideSteps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.GUIDE, "ko");

        if (guideSteps.isEmpty()) {
            return new GuideAdminResponse(null, "ko", spot.getTitle(), List.of());
        }

        SpotContentStep step = guideSteps.get(0);
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
        return new GuideAdminResponse(step.getId(), "ko", step.getTitle(), lineResponses);
    }

    @Transactional
    public GuideAdminResponse saveGuide(Long tourId, Long spotId, GuideSaveRequest req) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));

        String lang = req.language() != null && !req.language().isBlank() ? req.language() : "ko";
        List<SpotContentStep> existing = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spotId, StepKind.GUIDE, lang);

        SpotContentStep step;
        if (existing.isEmpty()) {
            step = SpotContentStep.create(spot, StepKind.GUIDE, 0);
            step.setTitle(req.stepTitle() != null ? req.stepTitle() : spot.getTitle());
            step = spotContentStepRepository.save(step);
        } else {
            step = existing.get(0);
            if (req.stepTitle() != null) {
                step.setTitle(req.stepTitle());
            }
            step = spotContentStepRepository.save(step);
            List<SpotScriptLine> oldLines = spotScriptLineRepository.findByStep_IdOrderBySeqAsc(step.getId());
            Set<Long> assetIdsToCheck = oldLines.stream()
                    .flatMap(l -> scriptLineAssetRepository.findByScriptLine_IdOrderBySortOrderAsc(l.getId()).stream())
                    .map(sla -> sla.getAsset().getId())
                    .collect(Collectors.toSet());
            spotScriptLineRepository.deleteAll(oldLines);
            deleteOrphanedMediaAssets(assetIdsToCheck);
        }

        int seq = 1;
        for (GuideLineRequest lineReq : req.lines()) {
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

        return getGuide(tourId, spotId);
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

    /**
     * SpotAsset, ChatTurnAsset에서 참조하지 않는 MediaAsset 삭제 (고아 에셋 정리)
     */
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

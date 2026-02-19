package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.domain.enums.StepNextAction;
import com.app.questofseoul.domain.enums.SpotAssetUsage;
import com.app.questofseoul.dto.tour.GuideSegmentResponse;
import com.app.questofseoul.dto.tour.SpotDetailResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.ScriptLineAssetRepository;
import com.app.questofseoul.repository.SpotAssetRepository;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.SpotScriptLineRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotGuideService {

    private static final int DEFAULT_DELAY_MS = 2000;

    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final SpotScriptLineRepository spotScriptLineRepository;
    private final ScriptLineAssetRepository scriptLineAssetRepository;
    private final SpotAssetRepository spotAssetRepository;

    @Transactional(readOnly = true)
    public GuideSegmentResponse getSpotGuide(Long spotId, String lang) {
        TourSpot spot = tourSpotRepository.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        String language = (lang != null && !lang.isBlank()) ? lang : "ko";

        List<SpotContentStep> guideSteps = spotContentStepRepository
                .findBySpotIdAndLanguageOrderByStepIndexAsc(spotId, language)
                .stream().filter(s -> s.getKind() == StepKind.GUIDE).toList();

        List<GuideSegmentResponse.SegmentItem> segments = new ArrayList<>();
        int segIdx = 1;
        StepNextAction lastNextAction = null;
        for (SpotContentStep step : guideSteps) {
            lastNextAction = step.getNextAction();
            var lines = spotScriptLineRepository.findByStep_IdOrderBySeqAsc(step.getId());
            for (var line : lines) {
                List<GuideSegmentResponse.AssetItem> assets = scriptLineAssetRepository
                        .findByScriptLine_IdOrderBySortOrderAsc(line.getId())
                        .stream()
                        .map(a -> new GuideSegmentResponse.AssetItem(
                                a.getAsset().getId(),
                                a.getAsset().getAssetType() != null ? a.getAsset().getAssetType().name() : "IMAGE",
                                a.getAsset().getUrl(),
                                a.getAsset().getMetadataJson()))
                        .toList();
                segments.add(new GuideSegmentResponse.SegmentItem(
                        line.getId(), segIdx++, line.getText(), null, assets, DEFAULT_DELAY_MS));
            }
        }

        String nextActionStr = lastNextAction != null ? lastNextAction.name() : null;
        return new GuideSegmentResponse(spotId, spot.getTitle(), nextActionStr, segments);
    }

    @Transactional(readOnly = true)
    public SpotDetailResponse getSpotDetail(Long spotId) {
        TourSpot spot = tourSpotRepository.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        String thumbnailUrl = spotAssetRepository
                .findFirstBySpot_IdAndUsageOrderBySortOrderAsc(spotId, SpotAssetUsage.THUMBNAIL)
                .map(sa -> sa.getAsset().getUrl())
                .orElseGet(() -> spotAssetRepository.findBySpot_IdOrderBySortOrderAsc(spotId).stream()
                        .findFirst()
                        .map(sa -> sa.getAsset().getUrl())
                        .orElse(null));
        return new SpotDetailResponse(
                spot.getId(),
                spot.getType().name(),
                spot.getTitle(),
                spot.getTitleKr(),
                spot.getDescription(),
                spot.getPronunciationUrl(),
                thumbnailUrl,
                spot.getLatitude(),
                spot.getLongitude(),
                spot.getAddress()
        );
    }
}

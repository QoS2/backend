package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.ScriptLineAsset;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.dto.tour.GuideSegmentResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.ScriptLineAssetRepository;
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

    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final SpotScriptLineRepository spotScriptLineRepository;
    private final ScriptLineAssetRepository scriptLineAssetRepository;

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
        for (SpotContentStep step : guideSteps) {
            var lines = spotScriptLineRepository.findByStep_IdOrderBySeqAsc(step.getId());
            for (var line : lines) {
                List<GuideSegmentResponse.AssetItem> media = scriptLineAssetRepository
                        .findByScriptLine_IdOrderBySortOrderAsc(line.getId())
                        .stream()
                        .map(a -> new GuideSegmentResponse.AssetItem(
                                a.getAsset().getId(),
                                a.getAsset().getUrl(),
                                a.getAsset().getMetadataJson()))
                        .toList();
                segments.add(new GuideSegmentResponse.SegmentItem(
                        line.getId(), segIdx++, line.getText(), null, media));
            }
        }

        return new GuideSegmentResponse(spotId, spot.getTitle(), segments);
    }
}

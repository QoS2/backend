package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.SpotScriptLine;
import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.domain.entity.TourKnowledgeEmbedding;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.SpotScriptLineRepository;
import com.app.questofseoul.repository.TourKnowledgeEmbeddingRepository;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourKnowledgeSyncService {

    private static final Logger log = LoggerFactory.getLogger(TourKnowledgeSyncService.class);

    private final TourRepository tourRepository;
    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final SpotScriptLineRepository spotScriptLineRepository;
    private final TourKnowledgeEmbeddingRepository embeddingRepository;
    private final EmbeddingService embeddingService;

    @Value("${OPENAI_API_KEY:}")
    private String openaiApiKey;

    @Transactional
    public int syncAll() {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            log.warn("OPENAI_API_KEY not set, skipping tour knowledge sync");
            return 0;
        }
        int count = 0;
        List<Tour> tours = tourRepository.findAll();
        for (Tour tour : tours) {
            count += syncTour(tour);
        }
        log.info("Tour knowledge sync completed: {} embeddings", count);
        return count;
    }

    @Transactional
    public int syncTour(Long tourId) {
        Tour tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) return 0;
        return syncTour(tour);
    }

    private int syncTour(Tour tour) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) return 0;
        embeddingRepository.deleteByTourId(tour.getId());
        int count = 0;

        String desc = tour.getDisplayDescription();
        if (desc != null && !desc.isBlank()) {
            String content = "투어: " + tour.getDisplayTitle() + "\n" + desc;
            float[] emb = embeddingService.embed(content);
            if (emb.length > 0) {
                embeddingRepository.save(TourKnowledgeEmbedding.create(
                    "TOUR", tour.getId(), tour.getId(), null, content, tour.getDisplayTitle(), emb));
                count++;
            }
        }

        List<TourSpot> spots = tourSpotRepository.findByTourIdOrderByOrderIndexAsc(tour.getId());
        for (TourSpot spot : spots) {
            String spotTitle = spot.getTitle();
            String spotDesc = spot.getDescription();
            String spotContent = "스팟: " + spotTitle;
            if (spotDesc != null && !spotDesc.isBlank()) spotContent += "\n" + spotDesc;
            if (spotContent.length() > 50) {
                float[] emb = embeddingService.embed(spotContent);
                if (emb.length > 0) {
                    embeddingRepository.save(TourKnowledgeEmbedding.create(
                        "SPOT", spot.getId(), tour.getId(), spot.getId(), spotContent, spotTitle, emb));
                    count++;
                }
            }

            List<SpotContentStep> steps = spotContentStepRepository
                .findBySpotIdAndLanguageOrderByStepIndexAsc(spot.getId(), "ko");
            for (SpotContentStep step : steps) {
                if (step.getKind() != StepKind.GUIDE) continue;
                List<SpotScriptLine> lines = spotScriptLineRepository.findByStep_IdOrderBySeqAsc(step.getId());
                for (SpotScriptLine line : lines) {
                    String text = line.getText();
                    if (text == null || text.isBlank()) continue;
                    String content = "[" + spotTitle + "] " + text;
                    float[] emb = embeddingService.embed(content);
                    if (emb.length > 0) {
                        embeddingRepository.save(TourKnowledgeEmbedding.create(
                            "GUIDE_LINE", line.getId(), tour.getId(), spot.getId(), content, spotTitle, emb));
                        count++;
                    }
                }
            }
        }
        return count;
    }
}

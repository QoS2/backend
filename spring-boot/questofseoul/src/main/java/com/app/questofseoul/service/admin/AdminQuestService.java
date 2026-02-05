package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.Quest;
import com.app.questofseoul.domain.enums.QuestTheme;
import com.app.questofseoul.dto.QuestResponse;
import com.app.questofseoul.dto.admin.QuestCreateRequest;
import com.app.questofseoul.dto.admin.QuestUpdateRequest;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminQuestService {

    private final QuestRepository questRepository;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional(readOnly = true)
    public Page<QuestResponse> list(Boolean isActive, QuestTheme theme, Pageable pageable) {
        Page<Quest> page;
        if (isActive != null && theme != null) {
            page = questRepository.findByIsActiveAndTheme(isActive, theme, pageable);
        } else if (isActive != null) {
            page = questRepository.findByIsActive(isActive, pageable);
        } else if (theme != null) {
            page = questRepository.findByTheme(theme, pageable);
        } else {
            page = questRepository.findAllBy(pageable);
        }
        return page.map(this::toQuestResponse);
    }

    @Transactional(readOnly = true)
    public Optional<QuestResponse> get(UUID questId) {
        return questRepository.findById(questId).map(this::toQuestResponse);
    }

    @Transactional(readOnly = true)
    public Quest getEntityOrThrow(UUID questId) {
        return questRepository.findById(questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트", questId));
    }

    @Transactional
    public QuestResponse create(QuestCreateRequest req) {
        var point = pointOrNull(req.getStartLocationLatitude(), req.getStartLocationLongitude());
        Quest quest = Quest.create(
            req.getTitle(),
            req.getSubtitle(),
            req.getTheme(),
            req.getTone(),
            req.getDifficulty(),
            req.getEstimatedMinutes(),
            point
        );
        quest = questRepository.save(quest);
        return toQuestResponse(quest);
    }

    @Transactional
    public QuestResponse update(UUID questId, QuestUpdateRequest req) {
        Quest quest = getEntityOrThrow(questId);
        if (req.getTitle() != null) quest.setTitle(req.getTitle());
        if (req.getSubtitle() != null) quest.setSubtitle(req.getSubtitle());
        if (req.getTheme() != null) quest.setTheme(req.getTheme());
        if (req.getTone() != null) quest.setTone(req.getTone());
        if (req.getDifficulty() != null) quest.setDifficulty(req.getDifficulty());
        if (req.getEstimatedMinutes() != null) quest.setEstimatedMinutes(req.getEstimatedMinutes());
        if (req.getStartLocationLatitude() != null && req.getStartLocationLongitude() != null) {
            quest.setStartLocation(pointOrNull(req.getStartLocationLatitude(), req.getStartLocationLongitude()));
        }
        if (req.getIsActive() != null) quest.setIsActive(req.getIsActive());
        quest = questRepository.save(quest);
        return toQuestResponse(quest);
    }

    @Transactional
    public void delete(UUID questId) {
        Quest quest = getEntityOrThrow(questId);
        questRepository.delete(quest);
    }

    @Transactional
    public QuestResponse setActive(UUID questId, boolean active) {
        Quest quest = getEntityOrThrow(questId);
        quest.setIsActive(active);
        quest = questRepository.save(quest);
        return toQuestResponse(quest);
    }

    private QuestResponse toQuestResponse(Quest quest) {
        var builder = QuestResponse.builder()
            .id(quest.getId())
            .title(quest.getTitle())
            .subtitle(quest.getSubtitle())
            .theme(quest.getTheme())
            .tone(quest.getTone())
            .difficulty(quest.getDifficulty())
            .estimatedMinutes(quest.getEstimatedMinutes())
            .isActive(quest.getIsActive())
            .createdAt(quest.getCreatedAt());
        if (quest.getStartLocation() != null) {
            builder.startLocationLongitude(quest.getStartLocation().getX())
                   .startLocationLatitude(quest.getStartLocation().getY());
        }
        return builder.build();
    }

    private static org.locationtech.jts.geom.Point pointOrNull(Double lat, Double lon) {
        if (lat == null || lon == null) return null;
        return GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
    }
}

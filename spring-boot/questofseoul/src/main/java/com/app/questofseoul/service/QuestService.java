package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Quest;
import com.app.questofseoul.dto.ArrivalCheckRequest;
import com.app.questofseoul.dto.ArrivalCheckResponse;
import com.app.questofseoul.dto.QuestResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestService {

    private final QuestRepository questRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private static final double ARRIVAL_THRESHOLD_METERS = 100.0; // 100m 도착 판정

    @Transactional(readOnly = true)
    public List<QuestResponse> getActiveQuests() {
        return questRepository.findByIsActiveTrue().stream()
            .map(this::toQuestResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<QuestResponse> getQuest(UUID questId) {
        return questRepository.findById(questId)
            .map(this::toQuestResponse);
    }

    private QuestResponse toQuestResponse(Quest quest) {
        QuestResponse.QuestResponseBuilder builder = QuestResponse.builder()
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

    @Transactional(readOnly = true)
    public ArrivalCheckResponse checkArrival(UUID questId, ArrivalCheckRequest request) {
        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트", questId));

        if (quest.getStartLocation() == null) {
            return ArrivalCheckResponse.builder()
                .isArrived(false)
                .distanceMeters(null)
                .canStart(false)
                .build();
        }

        Point userLocation = geometryFactory.createPoint(
            new Coordinate(request.getLongitude(), request.getLatitude()));

        // Calculate distance using PostGIS ST_Distance
        // Note: This is a simplified calculation. In production, use PostGIS ST_DistanceSphere
        double distance = calculateDistance(
            quest.getStartLocation().getY(), quest.getStartLocation().getX(),
            request.getLatitude(), request.getLongitude());

        boolean isArrived = distance <= ARRIVAL_THRESHOLD_METERS;

        return ArrivalCheckResponse.builder()
            .isArrived(isArrived)
            .distanceMeters(distance)
            .canStart(isArrived)
            .build();
    }

    // Haversine formula for distance calculation (meters)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

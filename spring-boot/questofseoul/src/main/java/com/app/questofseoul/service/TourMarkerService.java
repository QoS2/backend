package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.MarkerType;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.dto.tour.MarkerResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TourMarkerService {

    private final TourRepository tourRepository;
    private final TourSpotRepository tourSpotRepository;

    @Transactional(readOnly = true)
    public List<MarkerResponse> getMarkers(Long tourId, MarkerType filter) {
        tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        Stream<TourSpot> spots = tourSpotRepository.findByTourIdOrderByOrderIndexAsc(tourId).stream();

        if (filter != null) {
            SpotType spotType = toSpotType(filter);
            spots = spots.filter(s -> s.getType() == spotType);
        }

        return spots
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(s -> new MarkerResponse(
                        s.getId(),
                        toMarkerType(s.getType()),
                        s.getTitle(),
                        BigDecimal.valueOf(s.getLatitude()),
                        BigDecimal.valueOf(s.getLongitude()),
                        s.getRadiusM() != null ? s.getRadiusM() : 50,
                        s.getId(),
                        s.getOrderIndex() != null ? s.getOrderIndex() : 0))
                .toList();
    }

    private static SpotType toSpotType(MarkerType mt) {
        return switch (mt) {
            case STEP -> SpotType.MAIN;
            case WAYPOINT -> SpotType.SUB;
            case PHOTO_SPOT -> SpotType.PHOTO;
            case TREASURE -> SpotType.TREASURE;
        };
    }

    private static MarkerType toMarkerType(SpotType st) {
        return switch (st) {
            case MAIN -> MarkerType.STEP;
            case SUB -> MarkerType.WAYPOINT;
            case PHOTO -> MarkerType.PHOTO_SPOT;
            case TREASURE -> MarkerType.TREASURE;
        };
    }
}

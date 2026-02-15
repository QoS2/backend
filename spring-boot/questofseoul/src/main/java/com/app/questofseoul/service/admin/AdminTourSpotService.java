package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.dto.admin.SpotAdminResponse;
import com.app.questofseoul.dto.admin.SpotCreateRequest;
import com.app.questofseoul.dto.admin.SpotUpdateRequest;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTourSpotService {

    private final TourRepository tourRepository;
    private final TourSpotRepository tourSpotRepository;

    @Transactional(readOnly = true)
    public List<SpotAdminResponse> list(Long tourId) {
        return tourSpotRepository.findByTourIdOrderByOrderIndexAsc(tourId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpotAdminResponse get(Long tourId, Long spotId) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        return toResponse(spot);
    }

    @Transactional
    public SpotAdminResponse create(Long tourId, SpotCreateRequest req) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        TourSpot spot = TourSpot.create(tour, SpotType.valueOf(req.type()), req.title(),
                req.latitude(), req.longitude(), req.orderIndex());
        if (req.description() != null) spot.setDescription(req.description());
        if (req.titleKr() != null) spot.setTitleKr(req.titleKr());
        if (req.pronunciationUrl() != null) spot.setPronunciationUrl(req.pronunciationUrl());
        if (req.address() != null) spot.setAddress(req.address());
        if (req.radiusM() != null) spot.setRadiusM(req.radiusM());
        if (req.latitude() != null) spot.setLatitude(req.latitude());
        if (req.longitude() != null) spot.setLongitude(req.longitude());
        spot = tourSpotRepository.save(spot);
        return toResponse(spot);
    }

    @Transactional
    public SpotAdminResponse update(Long tourId, Long spotId, SpotUpdateRequest req) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        if (req.title() != null) spot.setTitle(req.title());
        if (req.titleKr() != null) spot.setTitleKr(req.titleKr());
        if (req.description() != null) spot.setDescription(req.description());
        if (req.pronunciationUrl() != null) spot.setPronunciationUrl(req.pronunciationUrl());
        if (req.address() != null) spot.setAddress(req.address());
        if (req.orderIndex() != null) spot.setOrderIndex(req.orderIndex());
        if (req.latitude() != null) spot.setLatitude(req.latitude());
        if (req.longitude() != null) spot.setLongitude(req.longitude());
        if (req.radiusM() != null) spot.setRadiusM(req.radiusM());
        spot = tourSpotRepository.save(spot);
        return toResponse(spot);
    }

    @Transactional
    public void delete(Long tourId, Long spotId) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        tourSpotRepository.delete(spot);
    }

    private SpotAdminResponse toResponse(TourSpot s) {
        return new SpotAdminResponse(s.getId(), s.getTour().getId(), s.getType().name(), s.getTitle(),
                s.getTitleKr(), s.getDescription(), s.getPronunciationUrl(), s.getAddress(),
                s.getLatitude(), s.getLongitude(), s.getRadiusM(), s.getOrderIndex());
    }
}

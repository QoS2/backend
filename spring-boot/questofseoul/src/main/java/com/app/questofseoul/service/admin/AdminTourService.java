package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.dto.admin.TourAdminResponse;
import com.app.questofseoul.dto.admin.TourCreateRequest;
import com.app.questofseoul.dto.admin.TourUpdateRequest;
import com.app.questofseoul.exception.DuplicateResourceException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminTourService {

    private final TourRepository tourRepository;
    private final StepRepository stepRepository;
    private final WaypointRepository waypointRepository;
    private final PhotoSpotRepository photoSpotRepository;
    private final TreasureRepository treasureRepository;
    private final QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public Page<TourAdminResponse> list(Pageable pageable) {
        return tourRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public TourAdminResponse get(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        return toResponse(tour);
    }

    @Transactional
    public TourAdminResponse create(TourCreateRequest req) {
        if (tourRepository.findByExternalKey(req.externalKey()).isPresent()) {
            throw new DuplicateResourceException("Tour externalKey", req.externalKey());
        }
        Tour tour = Tour.create(req.externalKey(), req.titleEn(), req.descriptionEn(), req.infoJson(), req.goodToKnowJson());
        tour = tourRepository.save(tour);
        return toResponse(tour);
    }

    @Transactional
    public TourAdminResponse update(Long tourId, TourUpdateRequest req) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        if (req.titleEn() != null) tour.setTitleEn(req.titleEn());
        if (req.descriptionEn() != null) tour.setDescriptionEn(req.descriptionEn());
        if (req.infoJson() != null) tour.setInfoJson(req.infoJson());
        if (req.goodToKnowJson() != null) tour.setGoodToKnowJson(req.goodToKnowJson());
        tour = tourRepository.save(tour);
        return toResponse(tour);
    }

    @Transactional
    public void delete(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        tourRepository.delete(tour);
    }

    private TourAdminResponse toResponse(Tour tour) {
        int steps = stepRepository.findByTourIdOrderByStepOrderAsc(tour.getId()).size();
        int waypoints = waypointRepository.findByTourId(tour.getId()).size();
        int photos = photoSpotRepository.findByTourId(tour.getId()).size();
        int treasures = treasureRepository.findByTourId(tour.getId()).size();
        int quizzes = (int) stepRepository.findByTourIdOrderByStepOrderAsc(tour.getId()).stream()
            .mapToLong(s -> quizRepository.findByStepIdOrderByIdAsc(s.getId()).size())
            .sum();
        return TourAdminResponse.from(tour.getId(), tour.getExternalKey(), tour.getTitleEn(), tour.getDescriptionEn(),
            tour.getInfoJson(), tour.getGoodToKnowJson(), steps, waypoints, photos, treasures, quizzes);
    }
}

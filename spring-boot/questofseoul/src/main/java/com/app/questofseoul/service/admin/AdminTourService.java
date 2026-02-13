package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.dto.admin.TourAdminResponse;
import com.app.questofseoul.dto.admin.TourCreateRequest;
import com.app.questofseoul.dto.admin.TourUpdateRequest;
import com.app.questofseoul.exception.DuplicateResourceException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminTourService {

    private final TourRepository tourRepository;
    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;

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
        int main = (int) tourSpotRepository.countByTourIdAndType(tour.getId(), SpotType.MAIN);
        int sub = (int) tourSpotRepository.countByTourIdAndType(tour.getId(), SpotType.SUB);
        int photos = (int) tourSpotRepository.countByTourIdAndType(tour.getId(), SpotType.PHOTO);
        int treasures = (int) tourSpotRepository.countByTourIdAndType(tour.getId(), SpotType.TREASURE);
        int missions = (int) spotContentStepRepository.countMissionsByTourId(tour.getId(), StepKind.MISSION);
        return TourAdminResponse.from(tour.getId(), tour.getExternalKey(), tour.getDisplayTitle(), tour.getDisplayDescription(),
                tour.getInfoJson(), tour.getGoodToKnowJson(), main, sub, photos, treasures, missions);
    }
}

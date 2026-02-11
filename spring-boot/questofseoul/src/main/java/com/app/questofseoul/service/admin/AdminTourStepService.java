package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.Step;
import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.dto.admin.StepAdminResponse;
import com.app.questofseoul.dto.admin.StepCreateRequest;
import com.app.questofseoul.dto.admin.StepUpdateRequest;
import com.app.questofseoul.exception.DuplicateResourceException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.StepRepository;
import com.app.questofseoul.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTourStepService {

    private final TourRepository tourRepository;
    private final StepRepository stepRepository;

    @Transactional(readOnly = true)
    public List<StepAdminResponse> list(Long tourId) {
        return stepRepository.findByTourIdOrderByStepOrderAsc(tourId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StepAdminResponse get(Long tourId, Long stepId) {
        Step step = stepRepository.findById(stepId)
            .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (!step.getTour().getId().equals(tourId)) {
            throw new ResourceNotFoundException("Step not in tour");
        }
        return toResponse(step);
    }

    @Transactional
    public StepAdminResponse create(Long tourId, StepCreateRequest req) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        if (stepRepository.findByExternalKey(req.externalKey()).isPresent()) {
            throw new DuplicateResourceException("Step externalKey", req.externalKey());
        }
        Step step = Step.create(tour, req.externalKey(), req.stepOrder(), req.titleEn(), req.shortDescEn(),
            req.latitude(), req.longitude(), req.radiusM());
        step = stepRepository.save(step);
        return toResponse(step);
    }

    @Transactional
    public StepAdminResponse update(Long tourId, Long stepId, StepUpdateRequest req) {
        Step step = stepRepository.findById(stepId)
            .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (!step.getTour().getId().equals(tourId)) {
            throw new ResourceNotFoundException("Step not in tour");
        }
        if (req.stepOrder() != null) step.setStepOrder(req.stepOrder());
        if (req.titleEn() != null) step.setTitleEn(req.titleEn());
        if (req.shortDescEn() != null) step.setShortDescEn(req.shortDescEn());
        if (req.latitude() != null) step.setLatitude(req.latitude());
        if (req.longitude() != null) step.setLongitude(req.longitude());
        if (req.radiusM() != null) step.setRadiusM(req.radiusM());
        return toResponse(stepRepository.save(step));
    }

    @Transactional
    public void delete(Long tourId, Long stepId) {
        Step step = stepRepository.findById(stepId)
            .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (!step.getTour().getId().equals(tourId)) {
            throw new ResourceNotFoundException("Step not in tour");
        }
        stepRepository.delete(step);
    }

    private StepAdminResponse toResponse(Step s) {
        return new StepAdminResponse(s.getId(), s.getExternalKey(), s.getTour().getId(), s.getStepOrder(),
            s.getTitleEn(), s.getShortDescEn(), s.getLatitude(), s.getLongitude(), s.getRadiusM());
    }
}

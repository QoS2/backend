package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.domain.entity.TourRun;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.entity.User;
import com.app.questofseoul.domain.enums.ProgressStatus;
import com.app.questofseoul.domain.enums.RunStatus;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.domain.enums.TourAccessStatus;
import com.app.questofseoul.dto.tour.RunRequest;
import com.app.questofseoul.dto.tour.RunResponse;
import com.app.questofseoul.dto.tour.NextSpotResponse;
import com.app.questofseoul.dto.tour.TourDetailResponse;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.TourRunRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import com.app.questofseoul.repository.UserRepository;
import com.app.questofseoul.repository.UserSpotProgressRepository;
import com.app.questofseoul.repository.UserTourAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourRunService {

    private final TourRepository tourRepository;
    private final TourSpotRepository tourSpotRepository;
    private final TourRunRepository tourRunRepository;
    private final UserTourAccessRepository userTourAccessRepository;
    private final UserRepository userRepository;
    private final UserSpotProgressRepository userSpotProgressRepository;

    @Transactional
    public RunResponse handleRun(java.util.UUID userId, Long tourId, RunRequest.RunMode mode) {
        if (mode == null) mode = RunRequest.RunMode.START;
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        // 403: user_tour_access.status != UNLOCKED
        if (!userTourAccessRepository.findByUserIdAndTourId(userId, tourId)
                .map(a -> a.getStatus() == TourAccessStatus.UNLOCKED)
                .orElse(false)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tour access not unlocked");
        }

        Optional<TourRun> inProgressOpt = tourRunRepository.findByUserIdAndTourIdAndStatus(userId, tourId, RunStatus.IN_PROGRESS);

        return switch (mode) {
            case START -> handleStart(userId, tour, inProgressOpt);
            case CONTINUE -> handleContinue(userId, tour, inProgressOpt);
        };
    }

    private RunResponse handleStart(java.util.UUID userId, Tour tour, Optional<TourRun> inProgressOpt) {
        if (inProgressOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already in progress - use CONTINUE");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TourRun run = TourRun.create(user, tour);
        run = tourRunRepository.save(run);
        return buildRunResponse(run, tour, RunRequest.RunMode.START);
    }

    private RunResponse handleContinue(java.util.UUID userId, Tour tour, Optional<TourRun> inProgressOpt) {
        if (inProgressOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No run in progress - use START");
        }
        TourRun run = inProgressOpt.get();
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }
        return buildRunResponse(run, tour, RunRequest.RunMode.CONTINUE);
    }

    private RunResponse buildRunResponse(TourRun run, Tour tour, RunRequest.RunMode mode) {
        TourSpot start = tour.getStartSpot();
        if (start != null && Boolean.FALSE.equals(start.getIsActive())) {
            start = null;
        }
        if (start == null) {
            var mainSpots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tour.getId(), SpotType.MAIN);
            if (!mainSpots.isEmpty()) start = mainSpots.get(0);
        }
        TourDetailResponse.StartSpotDto startSpot = null;
        if (start != null) {
            startSpot = TourDetailResponse.StartSpotDto.builder()
                    .spotId(start.getId())
                    .title(start.getTitle())
                    .lat(start.getLatitude())
                    .lng(start.getLongitude())
                    .radiusM(start.getRadiusM() != null ? start.getRadiusM() : 50)
                    .build();
        }

        // Progress
        List<Long> completedSpotIds = userSpotProgressRepository.findCompletedSpotIdsByTourRunId(
                run.getId(), ProgressStatus.COMPLETED);
        long totalProgressSpots = tourSpotRepository.countByTourIdAndType(tour.getId(), SpotType.MAIN)
                + tourSpotRepository.countByTourIdAndType(tour.getId(), SpotType.SUB);
        TourDetailResponse.ProgressDto progress = TourDetailResponse.ProgressDto.builder()
                .completedCount(completedSpotIds.size())
                .totalCount((int) totalProgressSpots)
                .completedSpotIds(completedSpotIds)
                .build();

        return RunResponse.builder()
                .runId(run.getId())
                .tourId(tour.getId())
                .status(run.getStatus().name())
                .mode(mode.name())
                .progress(progress)
                .startSpot(startSpot)
                .build();
    }

    @Transactional
    public NextSpotResponse getNextSpot(java.util.UUID userId, Long runId) {
        TourRun run = tourRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }

        List<TourSpot> routeSpots = tourSpotRepository.findByTourIdOrderByOrderIndexAsc(run.getTour().getId()).stream()
                .filter(s -> s.getType() == SpotType.MAIN || s.getType() == SpotType.SUB)
                .toList();

        Set<Long> doneSpotIds = userSpotProgressRepository.findByTourRunId(runId).stream()
                .filter(p -> p.getProgressStatus() == ProgressStatus.COMPLETED || p.getProgressStatus() == ProgressStatus.SKIPPED)
                .map(p -> p.getSpot().getId())
                .collect(Collectors.toSet());

        List<Long> completedSpotIds = routeSpots.stream()
                .map(TourSpot::getId)
                .filter(doneSpotIds::contains)
                .toList();

        TourSpot nextSpot = routeSpots.stream()
                .filter(s -> !doneSpotIds.contains(s.getId()))
                .findFirst()
                .orElse(null);

        boolean hasNextSpot = nextSpot != null;
        if (!hasNextSpot && run.getStatus() == RunStatus.IN_PROGRESS) {
            run.complete();
        }
        if (nextSpot != null) {
            ensureAndUnlockSpotProgress(run, nextSpot);
        }

        TourDetailResponse.ProgressDto progress = TourDetailResponse.ProgressDto.builder()
                .completedCount(completedSpotIds.size())
                .totalCount(routeSpots.size())
                .completedSpotIds(completedSpotIds)
                .build();

        NextSpotResponse.NextSpotDto nextSpotDto = null;
        if (nextSpot != null) {
            nextSpotDto = new NextSpotResponse.NextSpotDto(
                    nextSpot.getId(),
                    nextSpot.getType().name(),
                    nextSpot.getTitle(),
                    nextSpot.getLatitude(),
                    nextSpot.getLongitude(),
                    nextSpot.getRadiusM() != null ? nextSpot.getRadiusM() : 50,
                    nextSpot.getOrderIndex()
            );
        }

        return new NextSpotResponse(
                run.getId(),
                run.getStatus().name(),
                hasNextSpot,
                nextSpotDto,
                progress
        );
    }

    private void ensureAndUnlockSpotProgress(TourRun run, TourSpot spot) {
        var progress = userSpotProgressRepository.findByTourRunIdAndSpotId(run.getId(), spot.getId())
                .orElseGet(() -> userSpotProgressRepository.save(com.app.questofseoul.domain.entity.UserSpotProgress.create(run, spot)));
        progress.unlock();
    }
}

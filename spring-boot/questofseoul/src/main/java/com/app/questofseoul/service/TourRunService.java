package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.domain.entity.TourRun;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.entity.User;
import com.app.questofseoul.domain.enums.RunStatus;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.domain.enums.TourAccessStatus;
import com.app.questofseoul.dto.tour.RunRequest;
import com.app.questofseoul.dto.tour.RunResponse;
import com.app.questofseoul.dto.tour.TourDetailResponse;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.TourRepository;
import com.app.questofseoul.repository.TourRunRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import com.app.questofseoul.repository.UserRepository;
import com.app.questofseoul.repository.UserTourAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TourRunService {

    private final TourRepository tourRepository;
    private final TourSpotRepository tourSpotRepository;
    private final TourRunRepository tourRunRepository;
    private final UserTourAccessRepository userTourAccessRepository;
    private final UserRepository userRepository;

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
            case RESTART -> handleRestart(userId, tour, inProgressOpt);
        };
    }

    private RunResponse handleStart(java.util.UUID userId, Tour tour, Optional<TourRun> inProgressOpt) {
        if (inProgressOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already in progress - use CONTINUE");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TourRun run = TourRun.create(user, tour);
        run = tourRunRepository.save(run);
        return buildRunResponse(run, tour, RunRequest.RunMode.START, null);
    }

    private RunResponse handleContinue(java.util.UUID userId, Tour tour, Optional<TourRun> inProgressOpt) {
        if (inProgressOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No run in progress - use START");
        }
        TourRun run = inProgressOpt.get();
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }
        return buildRunResponse(run, tour, RunRequest.RunMode.CONTINUE, null);
    }

    private RunResponse handleRestart(java.util.UUID userId, Tour tour, Optional<TourRun> inProgressOpt) {
        TourRun previousRun = null;
        if (inProgressOpt.isPresent()) {
            TourRun r = inProgressOpt.get();
            if (!r.getUser().getId().equals(userId)) {
                throw new AuthorizationException("Not your tour run");
            }
            r.abandon();
            tourRunRepository.save(r);
            previousRun = r;
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TourRun newRun = TourRun.create(user, tour);
        newRun = tourRunRepository.save(newRun);
        return buildRunResponse(newRun, tour, RunRequest.RunMode.RESTART, previousRun);
    }

    private RunResponse buildRunResponse(TourRun run, Tour tour, RunRequest.RunMode mode, TourRun previousRun) {
        TourSpot start = tour.getStartSpot();
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
        RunResponse.PreviousRunDto prevDto = null;
        if (previousRun != null) {
            prevDto = RunResponse.PreviousRunDto.builder()
                    .runId(previousRun.getId())
                    .finalStatus(previousRun.getStatus().name())
                    .build();
        }
        return RunResponse.builder()
                .runId(run.getId())
                .tourId(tour.getId())
                .status(run.getStatus().name())
                .mode(mode.name())
                .previousRun(prevDto)
                .startSpot(startSpot)
                .build();
    }
}

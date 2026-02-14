package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.RunStatus;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.domain.enums.TourAccessStatus;
import com.app.questofseoul.dto.tour.TourDetailResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourDetailService {

    private final TourRepository tourRepository;
    private final TourSpotRepository tourSpotRepository;
    private final TourTagRepository tourTagRepository;
    private final UserTourAccessRepository userTourAccessRepository;
    private final TourRunRepository tourRunRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final UserSpotProgressRepository userSpotProgressRepository;

    @Transactional(readOnly = true)
    public TourDetailResponse getTourDetail(Long tourId, UUID userId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        // Access
        boolean hasAccess = false;
        TourAccessStatus accessStatus = TourAccessStatus.LOCKED;
        if (userId != null) {
            Optional<UserTourAccess> accessOpt = userTourAccessRepository.findByUserIdAndTourId(userId, tourId);
            if (accessOpt.isPresent() && accessOpt.get().getStatus() == TourAccessStatus.UNLOCKED) {
                hasAccess = true;
                accessStatus = TourAccessStatus.UNLOCKED;
            }
        }

        // Current run (IN_PROGRESS only)
        TourDetailResponse.CurrentRunDto currentRun = null;
        if (userId != null) {
            Optional<TourRun> runOpt = tourRunRepository.findByUserIdAndTourIdAndStatus(userId, tourId, RunStatus.IN_PROGRESS);
            if (runOpt.isPresent()) {
                TourRun run = runOpt.get();
                int completedSpots = (int) userSpotProgressRepository.countByTourRunIdAndProgressStatus(run.getId(), com.app.questofseoul.domain.enums.ProgressStatus.COMPLETED);
                List<TourSpot> allSpots = tourSpotRepository.findByTourIdOrderByOrderIndexAsc(tourId);
                currentRun = TourDetailResponse.CurrentRunDto.builder()
                        .runId(run.getId())
                        .status(run.getStatus().name())
                        .startedAt(run.getStartedAt() != null ? run.getStartedAt().toString() : null)
                        .progress(TourDetailResponse.ProgressDto.builder()
                                .completedSpots(completedSpots)
                                .totalSpots(allSpots.size())
                                .build())
                        .build();
            }
        }

        // Counts
        long mainCount = tourSpotRepository.countByTourIdAndType(tourId, SpotType.MAIN);
        long subCount = tourSpotRepository.countByTourIdAndType(tourId, SpotType.SUB);
        long photoCount = tourSpotRepository.countByTourIdAndType(tourId, SpotType.PHOTO);
        long treasureCount = tourSpotRepository.countByTourIdAndType(tourId, SpotType.TREASURE);
        long missionsCount = spotContentStepRepository.countMissionsByTourId(tourId, StepKind.MISSION);

        TourDetailResponse.CountsDto counts = TourDetailResponse.CountsDto.builder()
                .main((int) mainCount)
                .sub((int) subCount)
                .photo((int) photoCount)
                .treasure((int) treasureCount)
                .missions((int) missionsCount)
                .build();

        // Tags
        List<TourDetailResponse.TagDto> tags = tourTagRepository.findByTourId(tourId).stream()
                .map(tt -> TourDetailResponse.TagDto.builder()
                        .id(tt.getTag().getId())
                        .name(tt.getTag().getName())
                        .slug(tt.getTag().getSlug())
                        .build())
                .collect(Collectors.toList());

        // Info (from info_json)
        TourDetailResponse.InfoDto info = null;
        if (tour.getInfoJson() != null && !tour.getInfoJson().isEmpty()) {
            info = TourDetailResponse.InfoDto.builder()
                    .entrance_fee((Map<String, Object>) tour.getInfoJson().get("entrance_fee"))
                    .available_hours((List<Map<String, Object>>) tour.getInfoJson().get("available_hours"))
                    .estimated_duration_min(tour.getInfoJson().get("estimated_duration_min") != null
                            ? ((Number) tour.getInfoJson().get("estimated_duration_min")).intValue() : null)
                    .build();
        }

        // Good to know
        List<String> goodToKnow = new ArrayList<>();
        if (tour.getGoodToKnowJson() != null) {
            Object tips = tour.getGoodToKnowJson().get("tips");
            if (tips instanceof List) {
                for (Object t : (List<?>) tips) goodToKnow.add(String.valueOf(t));
            }
        }

        // Start spot
        TourDetailResponse.StartSpotDto startSpot = null;
        TourSpot start = tour.getStartSpot();
        if (start == null) {
            List<TourSpot> mainSpots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tourId, SpotType.MAIN);
            if (!mainSpots.isEmpty()) start = mainSpots.get(0);
        }
        if (start != null) {
            startSpot = TourDetailResponse.StartSpotDto.builder()
                    .spotId(start.getId())
                    .title(start.getTitle())
                    .lat(start.getLatitude())
                    .lng(start.getLongitude())
                    .radiusM(start.getRadiusM() != null ? start.getRadiusM() : 50)
                    .build();
        }

        // Map spots (MAIN + TREASURE for map display)
        List<TourDetailResponse.MapSpotDto> mapSpots = new ArrayList<>();
        List<TourSpot> mainSpots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tourId, SpotType.MAIN);
        List<TourSpot> treasureSpots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tourId, SpotType.TREASURE);
        for (TourSpot s : mainSpots) {
            if (s.getLatitude() != null && s.getLongitude() != null) {
                mapSpots.add(TourDetailResponse.MapSpotDto.builder()
                        .spotId(s.getId())
                        .type("MAIN")
                        .title(s.getTitle())
                        .lat(s.getLatitude())
                        .lng(s.getLongitude())
                        .build());
            }
        }
        for (TourSpot s : treasureSpots) {
            if (s.getLatitude() != null && s.getLongitude() != null) {
                mapSpots.add(TourDetailResponse.MapSpotDto.builder()
                        .spotId(s.getId())
                        .type("TREASURE")
                        .title(s.getTitle())
                        .lat(s.getLatitude())
                        .lng(s.getLongitude())
                        .build());
            }
        }

        // Actions
        String primaryButton;
        String secondaryButton = "GPS_TO_START";
        List<String> moreActions = new ArrayList<>();
        if (!hasAccess) {
            primaryButton = "UNLOCK";
        } else if (currentRun != null) {
            primaryButton = "CONTINUE";
            moreActions.add("RESTART");
        } else {
            primaryButton = "START";
        }

        TourDetailResponse.ActionsDto actions = TourDetailResponse.ActionsDto.builder()
                .primaryButton(primaryButton)
                .secondaryButton(secondaryButton)
                .moreActions(moreActions.isEmpty() ? null : moreActions)
                .build();

        // Main Quest Path (MAIN 스팟별 MISSION 스텝)
        List<TourDetailResponse.MainQuestPathItemDto> mainQuestPath = new ArrayList<>();
        int orderIdx = 1;
        for (TourSpot ms : mainSpots) {
            List<SpotContentStep> missionSteps = spotContentStepRepository
                    .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(ms.getId(), StepKind.MISSION, "ko");
            List<TourDetailResponse.QuestGameDto> games = new ArrayList<>();
            int gameIdx = 1;
            for (SpotContentStep s : missionSteps) {
                games.add(TourDetailResponse.QuestGameDto.builder()
                        .stepId(s.getId())
                        .missionId(s.getMission() != null ? s.getMission().getId() : null)
                        .title(s.getTitle() != null && !s.getTitle().isBlank() ? s.getTitle() : "Game " + gameIdx)
                        .build());
                gameIdx++;
            }
            mainQuestPath.add(TourDetailResponse.MainQuestPathItemDto.builder()
                    .spotId(ms.getId())
                    .spotTitle(ms.getTitle())
                    .orderIndex(orderIdx++)
                    .games(games)
                    .build());
        }

        return TourDetailResponse.builder()
                .tourId(tour.getId())
                .title(tour.getDisplayTitle())
                .description(tour.getDisplayDescription())
                .tags(tags)
                .counts(counts)
                .info(info)
                .goodToKnow(goodToKnow.isEmpty() ? null : goodToKnow)
                .startSpot(startSpot)
                .mapSpots(mapSpots)
                .access(TourDetailResponse.AccessDto.builder()
                        .status(accessStatus.name())
                        .hasAccess(hasAccess)
                        .build())
                .currentRun(currentRun)
                .actions(actions)
                .mainQuestPath(mainQuestPath.isEmpty() ? null : mainQuestPath)
                .build();
    }
}

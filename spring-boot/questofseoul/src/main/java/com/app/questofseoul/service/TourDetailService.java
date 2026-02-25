package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.RunStatus;
import com.app.questofseoul.domain.enums.SpotAssetUsage;
import com.app.questofseoul.domain.enums.TourAssetUsage;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.domain.enums.TourAccessStatus;
import com.app.questofseoul.dto.tour.TourDetailResponse;
import com.app.questofseoul.dto.tour.TourListItem;
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
    private final SpotAssetRepository spotAssetRepository;
    private final TourAssetRepository tourAssetRepository;

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
                List<Long> completedSpotIds = userSpotProgressRepository.findCompletedSpotIdsByTourRunId(run.getId(), com.app.questofseoul.domain.enums.ProgressStatus.COMPLETED);
                long totalProgressSpots = tourSpotRepository.countByTourIdAndType(tourId, SpotType.MAIN)
                        + tourSpotRepository.countByTourIdAndType(tourId, SpotType.SUB);
                currentRun = TourDetailResponse.CurrentRunDto.builder()
                        .runId(run.getId())
                        .status(run.getStatus().name())
                        .startedAt(run.getStartedAt() != null ? run.getStartedAt().toString() : null)
                        .progress(TourDetailResponse.ProgressDto.builder()
                                .completedCount(completedSpotIds.size())
                                .totalCount((int) totalProgressSpots)
                                .completedSpotIds(completedSpotIds)
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

        // Good to know (API 문서: 배열 ["팁1","팁2"] 또는 레거시 {"tips": ["팁1","팁2"]})
        List<String> goodToKnow = parseGoodToKnow(tour.getGoodToKnowJson());

        // Start spot
        TourDetailResponse.StartSpotDto startSpot = null;
        TourSpot start = tour.getStartSpot();
        if (start != null && Boolean.FALSE.equals(start.getIsActive())) {
            start = null;
        }
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

        // Map spots (MAIN + SUB + PHOTO + TREASURE for map display) - thumbnailUrl, isHighlight 포함
        List<TourDetailResponse.MapSpotDto> mapSpots = new ArrayList<>();
        List<TourSpot> mainSpots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tourId, SpotType.MAIN);
        List<TourSpot> subSpots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tourId, SpotType.SUB);
        List<TourSpot> photoSpots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tourId, SpotType.PHOTO);
        List<TourSpot> treasureSpots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tourId, SpotType.TREASURE);
        for (TourSpot s : mainSpots) {
            addMapSpot(mapSpots, s, "MAIN", true, true);
        }
        for (TourSpot s : subSpots) {
            addMapSpot(mapSpots, s, "SUB", false, true);
        }
        for (TourSpot s : photoSpots) {
            addMapSpot(mapSpots, s, "PHOTO", false, true);
        }
        for (TourSpot s : treasureSpots) {
            addMapSpot(mapSpots, s, "TREASURE", false, false);
        }

        // Actions
        String primaryButton;
        String secondaryButton = "GPS_TO_START";
        if (!hasAccess) {
            primaryButton = "UNLOCK";
        } else if (currentRun != null) {
            primaryButton = "CONTINUE";
        } else {
            primaryButton = "START";
        }

        TourDetailResponse.ActionsDto actions = TourDetailResponse.ActionsDto.builder()
                .primaryButton(primaryButton)
                .secondaryButton(secondaryButton)
                .moreActions(null)
                .build();

        // Main Mission Path (MAIN 스팟별 MISSION 스텝)
        List<TourDetailResponse.MainMissionPathItemDto> mainMissionPath = new ArrayList<>();
        int orderIdx = 1;
        for (TourSpot ms : mainSpots) {
            List<SpotContentStep> missionSteps = spotContentStepRepository
                    .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(ms.getId(), StepKind.MISSION, "ko");
            List<TourDetailResponse.MissionItemDto> missions = new ArrayList<>();
            int missionIdx = 1;
            for (SpotContentStep s : missionSteps) {
                missions.add(TourDetailResponse.MissionItemDto.builder()
                        .stepId(s.getId())
                        .missionId(s.getMission() != null ? s.getMission().getId() : null)
                        .title(s.getTitle() != null && !s.getTitle().isBlank() ? s.getTitle() : "Mission " + missionIdx)
                        .build());
                missionIdx++;
            }
            mainMissionPath.add(TourDetailResponse.MainMissionPathItemDto.builder()
                    .spotId(ms.getId())
                    .spotTitle(ms.getTitle())
                    .orderIndex(orderIdx++)
                    .missions(missions)
                    .build());
        }

        // 투어 디테일 썸네일: tour_assets 우선, 없으면 메인 플레이스 이미지로 fallback
        List<String> thumbnails = new ArrayList<>();
        List<TourAsset> tourAssets = tourAssetRepository.findByTour_IdOrderBySortOrderAsc(tourId);
        for (TourAsset ta : tourAssets) {
            if (ta.getUsage() == TourAssetUsage.THUMBNAIL || ta.getUsage() == TourAssetUsage.HERO_IMAGE
                    || ta.getUsage() == TourAssetUsage.GALLERY_IMAGE) {
                String url = ta.getAsset() != null ? ta.getAsset().getUrl() : null;
                if (url != null && !url.isBlank()) thumbnails.add(url);
            }
        }
        if (thumbnails.isEmpty()) {
            for (TourSpot ms : mainSpots) {
                List<SpotAsset> assets = spotAssetRepository.findBySpot_IdOrderBySortOrderAsc(ms.getId());
                for (SpotAsset sa : assets) {
                    if (sa.getUsage() == SpotAssetUsage.THUMBNAIL || sa.getUsage() == SpotAssetUsage.HERO_IMAGE
                            || sa.getUsage() == SpotAssetUsage.GALLERY_IMAGE) {
                        String url = sa.getAsset() != null ? sa.getAsset().getUrl() : null;
                        if (url != null && !url.isBlank()) thumbnails.add(url);
                    }
                }
            }
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
                .mainMissionPath(mainMissionPath.isEmpty() ? null : mainMissionPath)
                .thumbnails(thumbnails.isEmpty() ? null : thumbnails)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TourListItem> getTourList(UUID userId) {
        List<Tour> tours = tourRepository.findAll();
        return tours.stream().map(tour -> {
            Long tourId = tour.getId();

            // Counts
            long mainCount = tourSpotRepository.countByTourIdAndType(tourId, SpotType.MAIN);
            long subCount = tourSpotRepository.countByTourIdAndType(tourId, SpotType.SUB);
            long photoCount = tourSpotRepository.countByTourIdAndType(tourId, SpotType.PHOTO);
            long treasureCount = tourSpotRepository.countByTourIdAndType(tourId, SpotType.TREASURE);
            long missionsCount = spotContentStepRepository.countMissionsByTourId(tourId, StepKind.MISSION);

            TourDetailResponse.CountsDto counts = TourDetailResponse.CountsDto.builder()
                    .main((int) mainCount).sub((int) subCount)
                    .photo((int) photoCount).treasure((int) treasureCount)
                    .missions((int) missionsCount).build();

            // Tags
            List<TourDetailResponse.TagDto> tags = tourTagRepository.findByTourId(tourId).stream()
                    .map(tt -> TourDetailResponse.TagDto.builder()
                            .id(tt.getTag().getId())
                            .name(tt.getTag().getName())
                            .slug(tt.getTag().getSlug())
                            .build())
                    .collect(Collectors.toList());

            // estimatedDurationMin
            Integer estimatedDurationMin = null;
            if (tour.getInfoJson() != null && tour.getInfoJson().get("estimated_duration_min") != null) {
                estimatedDurationMin = ((Number) tour.getInfoJson().get("estimated_duration_min")).intValue();
            }

            // accessStatus
            String accessStatus = "LOCKED";
            if (userId != null) {
                accessStatus = userTourAccessRepository.findByUserIdAndTourId(userId, tourId)
                        .map(a -> a.getStatus().name())
                        .orElse("LOCKED");
            }

            // thumbnailUrl (tour_assets 우선, fallback to first main spot asset)
            String thumbnailUrl = null;
            List<TourAsset> tourAssets = tourAssetRepository.findByTour_IdOrderBySortOrderAsc(tourId);
            for (TourAsset ta : tourAssets) {
                if (ta.getUsage() == TourAssetUsage.THUMBNAIL || ta.getUsage() == TourAssetUsage.HERO_IMAGE) {
                    String url = ta.getAsset() != null ? ta.getAsset().getUrl() : null;
                    if (url != null && !url.isBlank()) { thumbnailUrl = url; break; }
                }
            }

            return TourListItem.builder()
                    .id(tour.getId())
                    .externalKey(tour.getExternalKey())
                    .title(tour.getDisplayTitle())
                    .thumbnailUrl(thumbnailUrl)
                    .description(tour.getDisplayDescription())
                    .counts(counts)
                    .estimatedDurationMin(estimatedDurationMin)
                    .accessStatus(accessStatus)
                    .tags(tags)
                    .build();
        }).collect(Collectors.toList());
    }

    /** 지도용 spot 공통 DTO 생성 (좌표 없는 스팟은 제외) */
    private void addMapSpot(List<TourDetailResponse.MapSpotDto> mapSpots,
                            TourSpot spot,
                            String type,
                            boolean isHighlight,
                            boolean includeThumbnail) {
        if (spot.getLatitude() == null || spot.getLongitude() == null) {
            return;
        }

        String thumbnailUrl = null;
        if (includeThumbnail) {
            thumbnailUrl = spotAssetRepository
                    .findFirstBySpot_IdAndUsageOrderBySortOrderAsc(spot.getId(), SpotAssetUsage.THUMBNAIL)
                    .map(sa -> sa.getAsset() != null ? sa.getAsset().getUrl() : null)
                    .orElse(null);
        }

        mapSpots.add(TourDetailResponse.MapSpotDto.builder()
                .spotId(spot.getId())
                .type(type)
                .title(spot.getTitle())
                .lat(spot.getLatitude())
                .lng(spot.getLongitude())
                .radius(spot.getRadiusM() != null ? spot.getRadiusM() : 50)
                .thumbnailUrl(thumbnailUrl)
                .isHighlight(isHighlight)
                .build());
    }

    /** good_to_know_json: {"tips": ["a","b"]} 또는 루트 배열 구조 파싱 */
    private List<String> parseGoodToKnow(Map<String, Object> goodToKnowJson) {
        List<String> result = new ArrayList<>();
        if (goodToKnowJson == null || goodToKnowJson.isEmpty()) return result;
        Object tips = goodToKnowJson.get("tips");
        if (tips instanceof List) {
            for (Object t : (List<?>) tips) result.add(String.valueOf(t));
            return result;
        }
        // JSON 배열 ["a","b"]가 Map으로 역직렬화되면 "0","1"... 키로 들어올 수 있음
        List<String> keys = new ArrayList<>(goodToKnowJson.keySet());
        keys.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareTo(b);
            }
        });
        for (String k : keys) {
            Object v = goodToKnowJson.get(k);
            if (v != null && !"tips".equals(k)) result.add(String.valueOf(v));
        }
        return result;
    }
}

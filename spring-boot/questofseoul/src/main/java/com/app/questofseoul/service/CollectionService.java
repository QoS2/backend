package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.SpotAssetUsage;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.domain.enums.TreasureStatus;
import com.app.questofseoul.dto.collection.*;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final UserSpotProgressRepository userSpotProgressRepository;
    private final UserTreasureStatusRepository userTreasureStatusRepository;
    private final TourSpotRepository tourSpotRepository;
    private final TourRepository tourRepository;
    private final TourRunRepository tourRunRepository;
    private final SpotAssetRepository spotAssetRepository;

    @Transactional(readOnly = true)
    public PlaceCollectionResponse getPlaceCollection(java.util.UUID userId, Long tourId) {
        List<UserSpotProgress> progresses = userSpotProgressRepository.findByUserIdAndUnlockedPlaces(userId, tourId);
        Map<Long, UserSpotProgress> latestBySpot = deduplicateBySpot(progresses);

        List<Long> tourIds = tourId != null ? List.of(tourId) : tourRunRepository.findDistinctTourIdsByUserId(userId);
        int totalAvailable = 0;
        for (Long tId : tourIds) {
            totalAvailable += tourSpotRepository.countByTourIdAndType(tId, SpotType.MAIN);
            totalAvailable += tourSpotRepository.countByTourIdAndType(tId, SpotType.SUB);
        }

        List<PlaceCollectionItemDto> items = latestBySpot.values().stream()
                .map(p -> toPlaceItem(p, true))
                .sorted(Comparator.comparing(PlaceCollectionItemDto::collectedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return new PlaceCollectionResponse(items.size(), Math.max(totalAvailable, items.size()), items);
    }

    @Transactional(readOnly = true)
    public PlaceCollectionSummaryResponse getPlaceCollectionSummary(java.util.UUID userId) {
        List<Long> tourIds = tourRunRepository.findDistinctTourIdsByUserId(userId);
        List<PlaceCollectionSummaryResponse.PlaceSummaryByTourDto> byTour = new ArrayList<>();
        int totalCollected = 0;
        int totalAvailable = 0;

        for (Long tId : tourIds) {
            Tour tour = tourRepository.findById(tId).orElse(null);
            if (tour == null) continue;
            long mainCount = tourSpotRepository.countByTourIdAndType(tId, SpotType.MAIN);
            long subCount = tourSpotRepository.countByTourIdAndType(tId, SpotType.SUB);
            int avail = (int) (mainCount + subCount);
            List<UserSpotProgress> list = userSpotProgressRepository.findByUserIdAndUnlockedPlaces(userId, tId);
            Set<Long> spotIds = new HashSet<>();
            for (UserSpotProgress p : list) spotIds.add(p.getSpot().getId());
            int collected = spotIds.size();
            totalCollected += collected;
            totalAvailable += avail;
            byTour.add(new PlaceCollectionSummaryResponse.PlaceSummaryByTourDto(tId, tour.getDisplayTitle(), collected, avail));
        }

        return new PlaceCollectionSummaryResponse(byTour, totalCollected, totalAvailable);
    }

    @Transactional(readOnly = true)
    public TreasureCollectionResponse getTreasureCollection(java.util.UUID userId, Long tourId) {
        List<UserTreasureStatus> statuses = userTreasureStatusRepository.findByUserIdAndCollected(userId, tourId);

        List<Long> tourIds = tourId != null ? List.of(tourId) : tourRunRepository.findDistinctTourIdsByUserId(userId);
        int totalAvailable = 0;
        for (Long tId : tourIds) {
            totalAvailable += tourSpotRepository.countByTourIdAndType(tId, SpotType.TREASURE);
        }

        List<TreasureCollectionItemDto> items = statuses.stream()
                .map(s -> toTreasureItem(s, true))
                .sorted(Comparator.comparing(TreasureCollectionItemDto::gotAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return new TreasureCollectionResponse(items.size(), Math.max(totalAvailable, items.size()), items);
    }

    @Transactional(readOnly = true)
    public TreasureCollectionSummaryResponse getTreasureCollectionSummary(java.util.UUID userId) {
        List<Long> tourIds = tourRunRepository.findDistinctTourIdsByUserId(userId);
        List<TreasureCollectionSummaryResponse.TreasureSummaryByTourDto> byTour = new ArrayList<>();
        int totalCollected = 0;
        int totalAvailable = 0;

        for (Long tId : tourIds) {
            Tour tour = tourRepository.findById(tId).orElse(null);
            if (tour == null) continue;
            int avail = (int) tourSpotRepository.countByTourIdAndType(tId, SpotType.TREASURE);
            List<UserTreasureStatus> list = userTreasureStatusRepository.findByUserIdAndCollected(userId, tId);
            totalCollected += list.size();
            totalAvailable += avail;
            byTour.add(new TreasureCollectionSummaryResponse.TreasureSummaryByTourDto(tId, tour.getDisplayTitle(), list.size(), avail));
        }

        return new TreasureCollectionSummaryResponse(byTour, totalCollected, totalAvailable);
    }

    @Transactional
    public void collectTreasure(java.util.UUID userId, Long runId, Long spotId) {
        TourRun run = tourRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }

        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, run.getTour().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        if (spot.getType() != SpotType.TREASURE) {
            throw new IllegalArgumentException("Spot is not a treasure");
        }

        UserTreasureStatus status = userTreasureStatusRepository.findByTourRunIdAndTreasureSpotId(runId, spotId)
                .orElseGet(() -> userTreasureStatusRepository.save(UserTreasureStatus.create(run, spot)));

        if (status.getStatus() == TreasureStatus.GET) {
            return; // 이미 수집됨
        }
        status.collect();
    }

    private Map<Long, UserSpotProgress> deduplicateBySpot(List<UserSpotProgress> progresses) {
        Map<Long, UserSpotProgress> map = new LinkedHashMap<>();
        for (UserSpotProgress p : progresses) {
            Long spotId = p.getSpot().getId();
            UserSpotProgress existing = map.get(spotId);
            LocalDateTime existingAt = existing != null ? (existing.getCompletedAt() != null ? existing.getCompletedAt() : existing.getUnlockedAt()) : null;
            LocalDateTime currentAt = p.getCompletedAt() != null ? p.getCompletedAt() : p.getUnlockedAt();
            if (existing == null || (currentAt != null && (existingAt == null || currentAt.isAfter(existingAt)))) {
                map.put(spotId, p);
            }
        }
        return map;
    }

    private PlaceCollectionItemDto toPlaceItem(UserSpotProgress p, boolean collected) {
        TourSpot spot = p.getSpot();
        LocalDateTime at = p.getCompletedAt() != null ? p.getCompletedAt() : p.getUnlockedAt();
        String thumb = getThumbnailUrl(spot.getId());
        return new PlaceCollectionItemDto(
                spot.getId(), spot.getTour().getId(), spot.getTour().getDisplayTitle(),
                spot.getType().name(), spot.getTitle(), spot.getDescription(),
                thumb, at, spot.getOrderIndex(), collected);
    }

    private TreasureCollectionItemDto toTreasureItem(UserTreasureStatus s, boolean collected) {
        TourSpot spot = s.getTreasureSpot();
        String thumb = getThumbnailUrl(spot.getId());
        return new TreasureCollectionItemDto(
                spot.getId(), spot.getTour().getId(), spot.getTour().getDisplayTitle(),
                spot.getTitle(), spot.getDescription(), thumb,
                s.getGotAt(), spot.getOrderIndex(), collected);
    }

    private String getThumbnailUrl(Long spotId) {
        return spotAssetRepository.findFirstBySpot_IdAndUsageOrderBySortOrderAsc(spotId, SpotAssetUsage.THUMBNAIL)
                .map(sa -> sa.getAsset().getUrl())
                .orElseGet(() -> spotAssetRepository.findBySpot_IdOrderBySortOrderAsc(spotId).stream()
                        .findFirst()
                        .map(sa -> sa.getAsset().getUrl())
                        .orElse(null));
    }
}

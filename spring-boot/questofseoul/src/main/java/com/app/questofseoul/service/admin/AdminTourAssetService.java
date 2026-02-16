package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.MediaAsset;
import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.domain.entity.TourAsset;
import com.app.questofseoul.domain.enums.AssetType;
import com.app.questofseoul.domain.enums.TourAssetUsage;
import com.app.questofseoul.dto.admin.TourAssetRequest;
import com.app.questofseoul.dto.admin.TourAssetResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.MediaAssetRepository;
import com.app.questofseoul.repository.TourAssetRepository;
import com.app.questofseoul.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTourAssetService {

    private final TourRepository tourRepository;
    private final TourAssetRepository tourAssetRepository;
    private final MediaAssetRepository mediaAssetRepository;

    @Transactional(readOnly = true)
    public List<TourAssetResponse> list(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        return tourAssetRepository.findByTour_IdOrderBySortOrderAsc(tour.getId()).stream()
                .map(ta -> new TourAssetResponse(
                        ta.getId(),
                        ta.getAsset().getId(),
                        ta.getAsset().getUrl(),
                        ta.getUsage().name(),
                        ta.getSortOrder(),
                        ta.getCaption()))
                .toList();
    }

    @Transactional
    public TourAssetResponse add(Long tourId, TourAssetRequest request) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        TourAssetUsage usage = TourAssetUsage.valueOf(request.usage());
        String mimeType = inferMimeType(request.url());
        MediaAsset asset = MediaAsset.create(AssetType.IMAGE, request.url(), mimeType);
        asset = mediaAssetRepository.save(asset);

        int sortOrder = request.sortOrder() != null ? request.sortOrder()
                : (int) (tourAssetRepository.findByTour_IdOrderBySortOrderAsc(tour.getId()).stream()
                        .filter(ta -> ta.getUsage() == usage)
                        .count() + 1);
        TourAsset tourAsset = TourAsset.create(tour, asset, usage, sortOrder);
        if (request.caption() != null) {
            tourAsset.setCaption(request.caption());
        }
        tourAsset = tourAssetRepository.save(tourAsset);

        return new TourAssetResponse(
                tourAsset.getId(),
                tourAsset.getAsset().getId(),
                tourAsset.getAsset().getUrl(),
                tourAsset.getUsage().name(),
                tourAsset.getSortOrder(),
                tourAsset.getCaption());
    }

    @Transactional
    public void delete(Long tourId, Long tourAssetId) {
        TourAsset ta = tourAssetRepository.findById(tourAssetId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour asset not found"));
        if (!ta.getTour().getId().equals(tourId)) {
            throw new ResourceNotFoundException("Tour asset not found");
        }
        tourAssetRepository.delete(ta);
    }

    private static String inferMimeType(String url) {
        if (url == null) return "image/jpeg";
        String lower = url.toLowerCase();
        if (lower.contains(".webp")) return "image/webp";
        if (lower.contains(".png")) return "image/png";
        if (lower.contains(".gif")) return "image/gif";
        return "image/jpeg";
    }
}

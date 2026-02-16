package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.MediaAsset;
import com.app.questofseoul.domain.entity.SpotAsset;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.AssetType;
import com.app.questofseoul.domain.enums.SpotAssetUsage;
import com.app.questofseoul.dto.admin.SpotAssetRequest;
import com.app.questofseoul.dto.admin.SpotAssetResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.MediaAssetRepository;
import com.app.questofseoul.repository.SpotAssetRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSpotAssetService {

    private final TourSpotRepository tourSpotRepository;
    private final SpotAssetRepository spotAssetRepository;
    private final MediaAssetRepository mediaAssetRepository;

    @Transactional(readOnly = true)
    public List<SpotAssetResponse> list(Long tourId, Long spotId) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        return spotAssetRepository.findBySpot_IdOrderBySortOrderAsc(spot.getId()).stream()
                .map(sa -> new SpotAssetResponse(
                        sa.getId(),
                        sa.getAsset().getId(),
                        sa.getAsset().getUrl(),
                        sa.getUsage().name(),
                        sa.getSortOrder(),
                        sa.getCaption()))
                .toList();
    }

    @Transactional
    public SpotAssetResponse add(Long tourId, Long spotId, SpotAssetRequest request) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        SpotAssetUsage usage = SpotAssetUsage.valueOf(request.usage());
        String mimeType = inferMimeType(request.url());
        MediaAsset asset = MediaAsset.create(AssetType.IMAGE, request.url(), mimeType);
        asset = mediaAssetRepository.save(asset);

        int sortOrder = request.sortOrder() != null ? request.sortOrder()
                : (int) (spotAssetRepository.findBySpot_IdOrderBySortOrderAsc(spot.getId()).stream()
                        .filter(sa -> sa.getUsage() == usage)
                        .count() + 1);
        SpotAsset spotAsset = SpotAsset.create(spot, asset, usage, sortOrder);
        if (request.caption() != null) {
            spotAsset.setCaption(request.caption());
        }
        spotAsset = spotAssetRepository.save(spotAsset);

        return new SpotAssetResponse(
                spotAsset.getId(),
                spotAsset.getAsset().getId(),
                spotAsset.getAsset().getUrl(),
                spotAsset.getUsage().name(),
                spotAsset.getSortOrder(),
                spotAsset.getCaption());
    }

    @Transactional
    public void delete(Long tourId, Long spotId, Long spotAssetId) {
        SpotAsset sa = spotAssetRepository.findById(spotAssetId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot asset not found"));
        if (!sa.getSpot().getTour().getId().equals(tourId) || !sa.getSpot().getId().equals(spotId)) {
            throw new ResourceNotFoundException("Spot asset not found");
        }
        spotAssetRepository.delete(sa);
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

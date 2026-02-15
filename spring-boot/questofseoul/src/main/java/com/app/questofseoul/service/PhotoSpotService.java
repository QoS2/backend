package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.AssetType;
import com.app.questofseoul.domain.enums.PhotoSubmissionStatus;
import com.app.questofseoul.domain.enums.SpotType;
import com.app.questofseoul.dto.photo.*;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoSpotService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final TourSpotRepository tourSpotRepository;
    private final SpotAssetRepository spotAssetRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final UserPhotoSubmissionRepository userPhotoSubmissionRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;

    @Transactional(readOnly = true)
    public List<PhotoSpotItemDto> getPhotoSpots(Long tourId) {
        List<TourSpot> spots;
        if (tourId != null) {
            spots = tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(tourId, SpotType.PHOTO);
        } else {
            spots = tourRepository.findAll().stream()
                    .flatMap(t -> tourSpotRepository.findByTourIdAndTypeOrderByOrderIndexAsc(t.getId(), SpotType.PHOTO).stream())
                    .toList();
        }
        return spots.stream().map(this::toPhotoSpotItem).toList();
    }

    @Transactional
    public PhotoSubmissionResponse submitPhoto(UUID userId, Long spotId, String photoUrl) {
        TourSpot spot = tourSpotRepository.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        if (spot.getType() != SpotType.PHOTO) {
            throw new IllegalArgumentException("Spot is not a photo spot");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        MediaAsset asset = MediaAsset.create(AssetType.IMAGE, photoUrl, "image/jpeg");
        asset = mediaAssetRepository.save(asset);

        UserPhotoSubmission submission = UserPhotoSubmission.create(user, spot, asset);
        submission = userPhotoSubmissionRepository.save(submission);

        return new PhotoSubmissionResponse(
                submission.getId(),
                submission.getStatus().name(),
                "검증 후 승인되면 민트 및 갤러리 노출됩니다.");
    }

    @Transactional(readOnly = true)
    public List<MyPhotoSubmissionItemDto> getMySubmissions(UUID userId, Long spotId, PhotoSubmissionStatus status) {
        List<UserPhotoSubmission> list;
        if (spotId != null && status != null) {
            list = userPhotoSubmissionRepository.findByUser_IdAndSpot_IdAndStatusOrderBySubmittedAtDesc(userId, spotId, status);
        } else if (spotId != null) {
            list = userPhotoSubmissionRepository.findByUser_IdAndSpot_IdOrderBySubmittedAtDesc(userId, spotId);
        } else if (status != null) {
            list = userPhotoSubmissionRepository.findByUser_IdAndStatusOrderBySubmittedAtDesc(userId, status);
        } else {
            list = userPhotoSubmissionRepository.findByUser_IdOrderBySubmittedAtDesc(userId);
        }

        return list.stream().map(s -> new MyPhotoSubmissionItemDto(
                s.getId(), s.getSpot().getId(), s.getSpot().getTitle(),
                s.getAsset().getUrl(), s.getStatus().name(),
                s.getSubmittedAt(), s.getRejectReason(),
                s.getVerifiedAt() != null && s.getStatus() == PhotoSubmissionStatus.APPROVED ? s.getVerifiedAt() : null
        )).toList();
    }

    private PhotoSpotItemDto toPhotoSpotItem(TourSpot spot) {
        String thumb = spotAssetRepository.findFirstBySpot_IdAndUsageOrderBySortOrderAsc(spot.getId(), com.app.questofseoul.domain.enums.SpotAssetUsage.THUMBNAIL)
                .map(sa -> sa.getAsset().getUrl())
                .orElseGet(() -> spotAssetRepository.findBySpot_IdOrderBySortOrderAsc(spot.getId()).stream()
                        .findFirst()
                        .map(sa -> sa.getAsset().getUrl())
                        .orElse(null));

        int userPhotoCount = (int) userPhotoSubmissionRepository.countBySpotIdAndStatus(spot.getId(), PhotoSubmissionStatus.APPROVED);
        List<UserPhotoSubmission> approved = userPhotoSubmissionRepository
                .findBySpot_IdAndStatusOrderBySubmittedAtDesc(spot.getId(), PhotoSubmissionStatus.APPROVED)
                .stream().limit(5).toList();

        List<PhotoSpotItemDto.SamplePhotoDto> samples = approved.stream()
                .map(s -> new PhotoSpotItemDto.SamplePhotoDto(
                        s.getId(),
                        s.getAsset().getUrl(),
                        s.getUser().getNickname() != null ? s.getUser().getNickname() : "user",
                        s.getVerifiedAt() != null ? s.getVerifiedAt().format(ISO) : null
                ))
                .toList();

        return new PhotoSpotItemDto(
                spot.getId(), spot.getTour().getId(), spot.getTour().getDisplayTitle(),
                spot.getTitle(), spot.getDescription(), thumb,
                spot.getLatitude(), spot.getLongitude(),
                userPhotoCount, samples);
    }

    @Transactional(readOnly = true)
    public PhotoSpotDetailResponse getPhotoSpotDetail(Long spotId) {
        TourSpot spot = tourSpotRepository.findById(spotId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        if (spot.getType() != SpotType.PHOTO) {
            throw new IllegalArgumentException("Spot is not a photo spot");
        }

        String thumb = spotAssetRepository.findFirstBySpot_IdAndUsageOrderBySortOrderAsc(spotId, com.app.questofseoul.domain.enums.SpotAssetUsage.THUMBNAIL)
                .map(sa -> sa.getAsset().getUrl())
                .orElseGet(() -> spotAssetRepository.findBySpot_IdOrderBySortOrderAsc(spotId).stream()
                        .findFirst()
                        .map(sa -> sa.getAsset().getUrl())
                        .orElse(null));

        List<PhotoSpotDetailResponse.OfficialPhotoDto> officialPhotos = spotAssetRepository
                .findBySpot_IdOrderBySortOrderAsc(spotId).stream()
                .filter(sa -> sa.getUsage() == com.app.questofseoul.domain.enums.SpotAssetUsage.GALLERY_IMAGE
                        || sa.getUsage() == com.app.questofseoul.domain.enums.SpotAssetUsage.HERO_IMAGE)
                .map(sa -> new PhotoSpotDetailResponse.OfficialPhotoDto(
                        sa.getAsset().getId(),
                        sa.getAsset().getUrl(),
                        sa.getCaption()))
                .toList();

        List<PhotoSpotDetailResponse.UserPhotoDto> userPhotos = userPhotoSubmissionRepository
                .findBySpot_IdAndStatusOrderBySubmittedAtDesc(spotId, PhotoSubmissionStatus.APPROVED)
                .stream()
                .map(s -> new PhotoSpotDetailResponse.UserPhotoDto(
                        s.getId(),
                        s.getAsset().getUrl(),
                        s.getUser().getNickname() != null ? s.getUser().getNickname() : "user",
                        s.getVerifiedAt() != null ? s.getVerifiedAt().format(ISO) : null))
                .toList();

        return new PhotoSpotDetailResponse(
                spot.getId(), spot.getTour().getId(), spot.getTour().getDisplayTitle(),
                spot.getTitle(), spot.getDescription(), thumb,
                spot.getLatitude(), spot.getLongitude(), spot.getAddress(),
                officialPhotos, userPhotos);
    }
}

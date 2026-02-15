package com.app.questofseoul.dto.photo;

import java.util.List;

public record PhotoSpotDetailResponse(
        Long spotId,
        Long tourId,
        String tourTitle,
        String title,
        String description,
        String thumbnailUrl,
        Double latitude,
        Double longitude,
        String address,
        List<OfficialPhotoDto> officialPhotos,
        List<UserPhotoDto> userPhotos
) {
    public record OfficialPhotoDto(Long id, String url, String caption) {}
    public record UserPhotoDto(Long submissionId, String url, String submittedBy, String mintedAt) {}
}

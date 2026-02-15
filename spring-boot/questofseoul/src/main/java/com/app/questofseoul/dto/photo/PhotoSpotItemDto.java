package com.app.questofseoul.dto.photo;

import java.util.List;

public record PhotoSpotItemDto(
        Long spotId,
        Long tourId,
        String tourTitle,
        String title,
        String description,
        String thumbnailUrl,
        Double latitude,
        Double longitude,
        int userPhotoCount,
        List<SamplePhotoDto> samplePhotos
) {
    public record SamplePhotoDto(Long id, String url, String submittedBy, String mintedAt) {}
}

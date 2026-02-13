package com.app.questofseoul.dto.admin;

import com.app.questofseoul.dto.tour.TourDetailResponse;

public record TourAdminResponse(
    Long id,
    String externalKey,
    String titleEn,
    String descriptionEn,
    java.util.Map<String, Object> infoJson,
    java.util.Map<String, Object> goodToKnowJson,
    int mainCount,
    int subCount,
    int photoSpotsCount,
    int treasuresCount,
    int missionsCount
) {
    public static TourAdminResponse from(Long id, String externalKey, String titleEn, String descriptionEn,
                                         java.util.Map<String, Object> infoJson, java.util.Map<String, Object> goodToKnowJson,
                                         int main, int waypoints, int photos, int treasures, int missions) {
        return new TourAdminResponse(id, externalKey, titleEn, descriptionEn, infoJson, goodToKnowJson,
            main, waypoints, photos, treasures, missions);
    }
}

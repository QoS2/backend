package com.app.questofseoul.dto.admin;

import com.app.questofseoul.dto.tour.TourDetailResponse;

public record TourAdminResponse(
    Long id,
    String externalKey,
    String titleEn,
    String descriptionEn,
    java.util.Map<String, Object> infoJson,
    java.util.Map<String, Object> goodToKnowJson,
    int stepsCount,
    int waypointsCount,
    int photoSpotsCount,
    int treasuresCount,
    int quizzesCount
) {
    public static TourAdminResponse from(Long id, String externalKey, String titleEn, String descriptionEn,
                                         java.util.Map<String, Object> infoJson, java.util.Map<String, Object> goodToKnowJson,
                                         int steps, int waypoints, int photos, int treasures, int quizzes) {
        return new TourAdminResponse(id, externalKey, titleEn, descriptionEn, infoJson, goodToKnowJson,
            steps, waypoints, photos, treasures, quizzes);
    }
}

package com.app.questofseoul.dto.tour;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record TourDetailResponse(
    Long id,
    String externalKey,
    String titleEn,
    String descriptionEn,
    Map<String, Object> infoJson,
    Map<String, Object> goodToKnowJson,
    List<String> tags,
    int stepsCount,
    int waypointsCount,
    int photoSpotsCount,
    int treasuresCount,
    int quizzesCount,
    BigDecimal startLatitude,
    BigDecimal startLongitude,
    boolean unlocked
) {}

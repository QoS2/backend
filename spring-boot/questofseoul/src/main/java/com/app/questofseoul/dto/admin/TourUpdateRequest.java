package com.app.questofseoul.dto.admin;

import java.util.Map;

public record TourUpdateRequest(
    String titleEn,
    String descriptionEn,
    Map<String, Object> infoJson,
    Map<String, Object> goodToKnowJson
) {}

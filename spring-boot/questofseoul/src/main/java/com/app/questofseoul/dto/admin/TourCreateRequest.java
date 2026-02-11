package com.app.questofseoul.dto.admin;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record TourCreateRequest(
    @NotBlank String externalKey,
    @NotBlank String titleEn,
    String descriptionEn,
    Map<String, Object> infoJson,
    Map<String, Object> goodToKnowJson
) {}

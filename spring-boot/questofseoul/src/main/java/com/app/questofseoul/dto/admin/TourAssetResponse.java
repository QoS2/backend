package com.app.questofseoul.dto.admin;

/** 투어 에셋 응답 */
public record TourAssetResponse(
    Long id,
    Long assetId,
    String url,
    String usage,
    Integer sortOrder,
    String caption
) {}

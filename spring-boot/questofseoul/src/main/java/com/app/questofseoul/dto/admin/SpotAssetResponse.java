package com.app.questofseoul.dto.admin;

/** 스팟 에셋 조회 응답 */
public record SpotAssetResponse(
    Long id,
    Long assetId,
    String url,
    String usage,
    Integer sortOrder,
    String caption
) {}

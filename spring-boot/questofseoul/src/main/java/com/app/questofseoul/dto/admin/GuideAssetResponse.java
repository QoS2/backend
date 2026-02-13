package com.app.questofseoul.dto.admin;

/**
 * 가이드 라인 첨부 미디어 응답
 */
public record GuideAssetResponse(
    Long id,
    String url,
    String assetType,
    String usage
) {}

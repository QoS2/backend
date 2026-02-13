package com.app.questofseoul.dto.admin;

import java.util.List;

/**
 * 가이드 한 문장 응답
 */
public record GuideLineResponse(
    Long id,
    Integer seq,
    String text,
    List<GuideAssetResponse> assets
) {}

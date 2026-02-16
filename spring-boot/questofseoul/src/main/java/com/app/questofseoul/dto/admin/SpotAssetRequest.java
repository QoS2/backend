package com.app.questofseoul.dto.admin;

/** 스팟 에셋 추가 요청 (url은 S3 업로드 API로 먼저 업로드) */
public record SpotAssetRequest(
    String url,
    String usage,
    Integer sortOrder,
    String caption
) {}

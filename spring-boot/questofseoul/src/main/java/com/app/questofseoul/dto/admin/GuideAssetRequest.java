package com.app.questofseoul.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 가이드 라인에 첨부할 미디어 에셋 (업로드된 S3 URL)
 */
public record GuideAssetRequest(
    @NotBlank String url,
    @NotNull String assetType,
    @NotNull String usage
) {}

package com.app.questofseoul.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 가이드 한 문장 (텍스트 + 첨부 미디어)
 */
public record GuideLineRequest(
    @NotBlank String text,
    @NotNull List<GuideAssetRequest> assets
) {
    public GuideLineRequest {
        assets = assets != null ? assets : List.of();
    }
}

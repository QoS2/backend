package com.app.questofseoul.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 스팟 가이드 저장 요청 (GUIDE step 전체 덮어쓰기)
 */
public record GuideSaveRequest(
    @NotNull String language,
    String stepTitle,
    @NotEmpty @Valid List<GuideLineRequest> lines
) {}

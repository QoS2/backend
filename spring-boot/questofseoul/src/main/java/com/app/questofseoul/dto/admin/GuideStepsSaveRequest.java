package com.app.questofseoul.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 스팟 가이드 전체 저장 요청 (N개 컨텐츠 블록 덮어쓰기)
 */
public record GuideStepsSaveRequest(
    @NotNull String language,
    @NotEmpty @Valid List<GuideStepSaveRequest> steps
) {}

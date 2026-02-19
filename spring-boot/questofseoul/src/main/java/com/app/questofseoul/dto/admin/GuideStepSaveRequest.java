package com.app.questofseoul.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 가이드 1개 스텝 (컨텐츠 블록) 저장 요청
 */
public record GuideStepSaveRequest(
    String stepTitle,
    String nextAction,
    Long missionStepId,
    @NotEmpty @Valid List<GuideLineRequest> lines
) {}

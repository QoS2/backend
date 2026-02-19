package com.app.questofseoul.dto.admin;

import java.util.List;

/**
 * 가이드 1개 스텝 (컨텐츠 블록) 응답
 */
public record GuideStepAdminResponse(
    Long stepId,
    Integer stepIndex,
    String stepTitle,
    String nextAction,
    Long missionStepId,
    List<GuideLineResponse> lines
) {}

package com.app.questofseoul.dto.admin;

import java.util.List;

/**
 * 스팟 가이드 전체 조회 응답 (N개 컨텐츠 블록)
 */
public record GuideStepsAdminResponse(
    String language,
    List<GuideStepAdminResponse> steps
) {}

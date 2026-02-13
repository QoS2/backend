package com.app.questofseoul.dto.admin;

import java.util.List;

/**
 * 스팟 가이드 관리자 조회 응답
 */
public record GuideAdminResponse(
    Long stepId,
    String language,
    String stepTitle,
    List<GuideLineResponse> lines
) {}

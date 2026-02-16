package com.app.questofseoul.dto.tour;

import java.util.Map;

/**
 * MISSION 스텝 상세 (프로시밍 후 미션 UI용).
 * prompt: 문제, optionsJson: 보기/이미지 (MISSION_SCHEMA.md 참조)
 */
public record MissionStepDetailResponse(
    Long stepId,
    Long missionId,
    String missionType,
    String prompt,
    Map<String, Object> optionsJson,
    String title
) {}

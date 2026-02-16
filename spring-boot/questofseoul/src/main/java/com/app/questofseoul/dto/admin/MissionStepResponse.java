package com.app.questofseoul.dto.admin;

import java.util.Map;

/** MISSION 스텝 조회 응답 */
public record MissionStepResponse(
    Long stepId,
    Long missionId,
    String missionType,
    String prompt,
    Map<String, Object> optionsJson,
    Map<String, Object> answerJson,
    String title,
    Integer stepIndex
) {}

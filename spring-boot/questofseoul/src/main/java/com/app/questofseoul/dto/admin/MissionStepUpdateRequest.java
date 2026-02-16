package com.app.questofseoul.dto.admin;

import java.util.Map;

/** MISSION 스텝 수정 요청 */
public record MissionStepUpdateRequest(
    String prompt,
    Map<String, Object> optionsJson,
    Map<String, Object> answerJson,
    String title
) {}

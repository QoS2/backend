package com.app.questofseoul.dto.admin;

import java.util.Map;

/** MISSION 스텝 추가 요청 (Mission + SpotContentStep 동시 생성) */
public record MissionStepCreateRequest(
    String missionType,
    String prompt,
    Map<String, Object> optionsJson,
    Map<String, Object> answerJson,
    String title
) {}

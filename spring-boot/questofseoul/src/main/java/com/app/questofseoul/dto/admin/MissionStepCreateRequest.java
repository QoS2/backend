package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.MissionType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/** MISSION 스텝 추가 요청 (Mission + SpotContentStep 동시 생성) */
public record MissionStepCreateRequest(
    @NotNull MissionType missionType,
    String prompt,
    Map<String, Object> optionsJson,
    Map<String, Object> answerJson,
    String title
) {}

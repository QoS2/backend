package com.app.questofseoul.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/** missions.mission_type */
public enum MissionType {
    QUIZ,
    OX,
    PHOTO,
    TEXT_INPUT;

    /** API 입력값을 표준 enum 값으로 정규화 */
    @JsonCreator
    public static MissionType fromApiValue(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("missionType must not be blank");
        }
        return MissionType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}

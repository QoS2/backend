package com.app.questofseoul.dto.tour;

import com.app.questofseoul.domain.enums.QuizType;

import java.util.Map;

public record QuizResponse(
    Long id,
    String externalKey,
    QuizType type,
    String promptEn,
    Map<String, Object> specJson,
    String hintEn,
    Integer mintReward,
    boolean hasHint
) {}

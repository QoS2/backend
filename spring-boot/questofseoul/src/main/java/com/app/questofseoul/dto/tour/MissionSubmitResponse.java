package com.app.questofseoul.dto.tour;

public record MissionSubmitResponse(
    Long attemptId,
    boolean success,
    Boolean isCorrect,
    Integer score,
    String feedback
) {}

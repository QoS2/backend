package com.app.questofseoul.dto.tour;

public record MissionSubmitResponse(
    Long attemptId,
    Boolean isCorrect,
    Integer score,
    String feedback,
    String nextStepApi
) {}

package com.app.questofseoul.dto.tour;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatSessionStatusResponse(
    Long sessionId,
    String status,
    Long lastTurnId
) {}

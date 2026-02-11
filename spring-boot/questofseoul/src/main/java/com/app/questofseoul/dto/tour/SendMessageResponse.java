package com.app.questofseoul.dto.tour;

public record SendMessageResponse(
    Long userTurnId,
    String userText,
    Long aiTurnId,
    String aiText
) {}

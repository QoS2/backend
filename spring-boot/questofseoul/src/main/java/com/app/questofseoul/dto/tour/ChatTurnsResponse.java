package com.app.questofseoul.dto.tour;

import java.util.List;

public record ChatTurnsResponse(
    Long sessionId,
    List<ChatTurnItem> turns
) {
    public record ChatTurnItem(
        Long id,
        String role,
        String source,
        String text,
        Object assets,
        Object action,
        String createdAt
    ) {}
}

package com.app.questofseoul.dto.tour;

import java.util.List;

public record ChatTurnsResponse(
    Long sessionId,
    String status,
    String nextScriptApi,
    Boolean hasNextScript,
    List<ChatTurnItem> turns
) {
    public record ChatTurnItem(
        Long turnId,
        String role,
        String source,
        String text,
        List<AssetDto> assets,
        Integer delayMs,
        ActionDto action,
        String createdAt
    ) {}
    public record AssetDto(Long id, String type, String url, Object meta) {}
    public record ActionDto(String type, String nextApi) {}
}

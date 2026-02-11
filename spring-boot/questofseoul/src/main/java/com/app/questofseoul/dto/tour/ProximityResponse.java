package com.app.questofseoul.dto.tour;

import java.util.List;

public record ProximityResponse(
    String event,
    String contentType,
    Long sessionId,
    ProximityContext context,
    List<ChatTurnDto> messages
) {
    public record ProximityContext(String refType, Long refId, String placeName) {}
    public record ChatTurnDto(Long turnId, String role, String source, String text, List<AssetDto> assets, ActionDto action) {}
    public record AssetDto(Long id, String type, String url, Object meta) {}
    public record ActionDto(String type, String label, Long stepId) {}
}

package com.app.questofseoul.dto.tour;

import java.util.List;

public record ProximityResponse(
    String event,
    String contentType,
    Long sessionId,
    ProximityContext context,
    ChatTurnDto message
) {
    public record ProximityContext(String refType, Long refId, String placeName, String spotType) {
        public ProximityContext(String refType, Long refId, String placeName) {
            this(refType, refId, placeName, null);
        }
    }
    public record ChatTurnDto(Long turnId, String role, String source, String text, List<AssetDto> assets, Integer delayMs, ActionDto action) {}
    public record AssetDto(Long id, String type, String url, Object meta) {}
    public record ActionDto(String type, String nextApi, String label, Long stepId) {}

    /** Treasure/Photo Spot 근접 알람용 (message 없음) */
    public static ProximityResponse treasureFound(Long spotId, String title) {
        return new ProximityResponse("TREASURE_FOUND", "TREASURE_ALARM", null,
                new ProximityContext("SPOT", spotId, title, "TREASURE"), null);
    }

    /** Photo Spot 근접 알람용 */
    public static ProximityResponse photoSpotFound(Long spotId, String title) {
        return new ProximityResponse("PHOTO_SPOT_FOUND", "PHOTO_ALARM", null,
                new ProximityContext("SPOT", spotId, title, "PHOTO"), null);
    }
}

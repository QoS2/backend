package com.app.questofseoul.dto.tour;

import java.util.List;

public record GuideSegmentResponse(
    Long stepId,
    String stepTitle,
    String nextAction,
    List<SegmentItem> segments
) {
    public record SegmentItem(
        Long id,
        Integer segIdx,
        String text,
        String triggerKey,
        List<AssetItem> assets,
        Integer delayMs
    ) {}
    public record AssetItem(Long id, String type, String url, Object meta) {}
}

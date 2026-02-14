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
        String textEn,
        String triggerKey,
        List<AssetItem> media
    ) {}
    public record AssetItem(Long id, String url, Object meta) {}
}

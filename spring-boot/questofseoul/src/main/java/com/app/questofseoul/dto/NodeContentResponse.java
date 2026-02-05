package com.app.questofseoul.dto;

import com.app.questofseoul.domain.enums.DisplayMode;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class NodeContentResponse {
    private UUID contentId;
    private Integer contentOrder;
    private TextContent text;
    private AudioContent audio;
    private UIHints uiHints;

    @Getter
    @Builder
    public static class TextContent {
        private String script;
        private DisplayMode displayMode;
    }

    @Getter
    @Builder
    public static class AudioContent {
        private String audioUrl;
        private Integer durationSec;
        private Boolean autoPlay;
    }

    @Getter
    @Builder
    public static class UIHints {
        private Boolean showSubtitle;
        private Boolean allowSpeedControl;
        private Boolean allowReplay;
    }
}

package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.ContentType;
import com.app.questofseoul.domain.enums.DisplayMode;
import com.app.questofseoul.domain.enums.Language;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ContentResponse {
    private UUID id;
    private UUID nodeId;
    private Integer contentOrder;
    private ContentType contentType;
    private Language language;
    private String body;
    private String audioUrl;
    private String voiceStyle;
    private DisplayMode displayMode;
    private LocalDateTime createdAt;
}

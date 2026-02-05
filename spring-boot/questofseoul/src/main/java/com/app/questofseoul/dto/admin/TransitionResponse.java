package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.domain.enums.TransitionMessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TransitionResponse {
    private UUID id;
    private UUID fromNodeId;
    private UUID toNodeId;
    private Integer transitionOrder;
    private TransitionMessageType messageType;
    private String textContent;
    private String audioUrl;
    private Language language;
    private LocalDateTime createdAt;
}

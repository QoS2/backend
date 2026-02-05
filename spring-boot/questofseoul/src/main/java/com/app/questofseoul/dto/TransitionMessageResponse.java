package com.app.questofseoul.dto;

import com.app.questofseoul.domain.enums.TransitionMessageType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TransitionMessageResponse {
    private UUID fromNodeId;
    private UUID toNodeId;
    private List<Message> messages;

    @Getter
    @Builder
    public static class Message {
        private Integer transitionOrder;
        private TransitionMessageType messageType;
        private String textContent;
        private String audioUrl;
    }
}

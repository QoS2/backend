package com.app.questofseoul.dto;

import com.app.questofseoul.domain.enums.ActionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class QuestHistoryResponse {
    private UUID questId;
    private String questTitle;
    private List<NodeHistory> history;

    @Getter
    @Builder
    public static class NodeHistory {
        private UUID nodeId;
        private String nodeTitle;
        private List<ActionHistory> actions;
    }

    @Getter
    @Builder
    public static class ActionHistory {
        private ActionType actionType;
        private String userInput;
        private String photoUrl;
        private Map<String, Object> selectedOption;
        private LocalDateTime createdAt;
    }
}

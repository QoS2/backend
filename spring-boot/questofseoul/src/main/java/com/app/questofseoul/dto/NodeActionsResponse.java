package com.app.questofseoul.dto;

import com.app.questofseoul.domain.enums.ActionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class NodeActionsResponse {
    private UUID nodeId;
    private String nodeTitle;
    private List<ActionInfo> actions;

    @Getter
    @Builder
    public static class ActionInfo {
        private UUID actionId;
        private ActionType actionType;
        private String prompt;
        private Map<String, Object> options;
    }
}

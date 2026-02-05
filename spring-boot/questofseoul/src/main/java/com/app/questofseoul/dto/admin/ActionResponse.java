package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.ActionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class ActionResponse {
    private UUID id;
    private UUID nodeId;
    private ActionType actionType;
    private String prompt;
    private Map<String, Object> options;
    private LocalDateTime createdAt;
    private List<EffectResponse> effects;
}

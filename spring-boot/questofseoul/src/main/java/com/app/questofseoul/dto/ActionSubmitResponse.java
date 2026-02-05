package com.app.questofseoul.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ActionSubmitResponse {
    private Boolean success;
    private List<EffectResponse> effects;
    private UUID nextNodeUnlocked;

    @Getter
    @Builder
    public static class EffectResponse {
        private String type;
        private Object value;
    }
}

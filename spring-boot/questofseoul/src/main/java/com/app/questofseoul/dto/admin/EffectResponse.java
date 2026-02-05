package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.EffectType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class EffectResponse {
    private UUID id;
    private UUID actionId;
    private EffectType effectType;
    private Map<String, Object> effectValue;
    private LocalDateTime createdAt;
}

package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.EffectType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class EffectUpdateRequest {

    private EffectType effectType;
    private Map<String, Object> effectValue;
}

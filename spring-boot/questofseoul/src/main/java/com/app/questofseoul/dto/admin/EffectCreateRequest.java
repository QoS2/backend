package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.EffectType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class EffectCreateRequest {

    @NotNull(message = "이펙트 타입은 필수입니다")
    private EffectType effectType;

    @NotNull(message = "이펙트 값은 필수입니다")
    private Map<String, Object> effectValue;
}

package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ActionCreateRequest {

    @NotNull(message = "액션 타입은 필수입니다")
    private ActionType actionType;

    @NotNull(message = "프롬프트는 필수입니다")
    private String prompt;

    private Map<String, Object> options;
}

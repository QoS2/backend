package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.ActionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ActionUpdateRequest {

    private ActionType actionType;
    private String prompt;
    private Map<String, Object> options;
}

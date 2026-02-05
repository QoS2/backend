package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.domain.enums.TransitionMessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransitionUpdateRequest {

    private Integer transitionOrder;
    private TransitionMessageType messageType;
    private String textContent;
    private String audioUrl;
    private Language language;
}

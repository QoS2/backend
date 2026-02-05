package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.domain.enums.TransitionMessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TransitionCreateRequest {

    @NotNull(message = "출발 노드 ID는 필수입니다")
    private UUID fromNodeId;

    @NotNull(message = "도착 노드 ID는 필수입니다")
    private UUID toNodeId;

    @NotNull(message = "전환 순서는 필수입니다")
    private Integer transitionOrder;

    @NotNull(message = "메시지 타입은 필수입니다")
    private TransitionMessageType messageType;

    private String textContent;
    private String audioUrl;
    private Language language;
}

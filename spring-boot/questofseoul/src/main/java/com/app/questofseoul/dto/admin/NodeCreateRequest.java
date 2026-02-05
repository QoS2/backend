package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.NodeType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class NodeCreateRequest {

    @NotNull(message = "노드 타입은 필수입니다")
    private NodeType nodeType;

    @NotNull(message = "제목은 필수입니다")
    private String title;

    @NotNull(message = "순서는 필수입니다")
    private Integer orderIndex;

    private Double geoLatitude;
    private Double geoLongitude;

    private Map<String, Object> unlockCondition;
}

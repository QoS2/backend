package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.NodeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class NodeUpdateRequest {

    private NodeType nodeType;
    private String title;
    private Integer orderIndex;
    private Double geoLatitude;
    private Double geoLongitude;
    private Map<String, Object> unlockCondition;
}

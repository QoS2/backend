package com.app.questofseoul.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class NodeContentsResponse {
    private UUID nodeId;
    private String title;
    private List<NodeContentResponse> contents;
    private Integer totalContents;
    private Integer currentContentOrder;
}

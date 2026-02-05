package com.app.questofseoul.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class NodeReorderRequest {

    @NotNull(message = "노드 순서 목록은 필수입니다")
    private List<NodeOrderItem> nodes;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class NodeOrderItem {
        @NotNull
        private UUID nodeId;
        @NotNull
        private Integer orderIndex;
    }
}

package com.app.questofseoul.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class QuestCompletionResponse {
    private UUID questId;
    private String questTitle;
    private LocalDateTime completedAt;
    private Boolean reportReady;
    private String reportUrl; // AI 리포트 URL (FastAPI에서 생성)
}

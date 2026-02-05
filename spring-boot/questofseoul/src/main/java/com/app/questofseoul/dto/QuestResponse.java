package com.app.questofseoul.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.app.questofseoul.domain.enums.Difficulty;
import com.app.questofseoul.domain.enums.QuestTheme;
import com.app.questofseoul.domain.enums.QuestTone;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestResponse {
    private UUID id;
    private String title;
    private String subtitle;
    private QuestTheme theme;
    private QuestTone tone;
    private Difficulty difficulty;
    private Integer estimatedMinutes;
    private Double startLocationLongitude;
    private Double startLocationLatitude;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

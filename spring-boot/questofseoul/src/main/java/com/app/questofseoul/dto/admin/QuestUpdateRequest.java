package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.Difficulty;
import com.app.questofseoul.domain.enums.QuestTheme;
import com.app.questofseoul.domain.enums.QuestTone;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestUpdateRequest {

    private String title;
    private String subtitle;
    private QuestTheme theme;
    private QuestTone tone;
    private Difficulty difficulty;
    private Integer estimatedMinutes;
    private Double startLocationLatitude;
    private Double startLocationLongitude;
    private Boolean isActive;
}

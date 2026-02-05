package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.Difficulty;
import com.app.questofseoul.domain.enums.QuestTheme;
import com.app.questofseoul.domain.enums.QuestTone;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestCreateRequest {

    @NotNull(message = "제목은 필수입니다")
    private String title;

    private String subtitle;

    @NotNull(message = "테마는 필수입니다")
    private QuestTheme theme;

    @NotNull(message = "톤은 필수입니다")
    private QuestTone tone;

    @NotNull(message = "난이도는 필수입니다")
    private Difficulty difficulty;

    private Integer estimatedMinutes;

    private Double startLocationLatitude;
    private Double startLocationLongitude;
}

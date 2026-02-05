package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.ContentType;
import com.app.questofseoul.domain.enums.DisplayMode;
import com.app.questofseoul.domain.enums.Language;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContentCreateRequest {

    @NotNull(message = "콘텐츠 순서는 필수입니다")
    private Integer contentOrder;

    @NotNull(message = "콘텐츠 타입은 필수입니다")
    private ContentType contentType;

    @NotNull(message = "언어는 필수입니다")
    private Language language;

    @NotNull(message = "본문은 필수입니다")
    private String body;

    private String audioUrl;
    private String voiceStyle;
    private DisplayMode displayMode;
}

package com.app.questofseoul.dto.admin;

import com.app.questofseoul.domain.enums.ContentType;
import com.app.questofseoul.domain.enums.DisplayMode;
import com.app.questofseoul.domain.enums.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContentUpdateRequest {

    private Integer contentOrder;
    private ContentType contentType;
    private Language language;
    private String body;
    private String audioUrl;
    private String voiceStyle;
    private DisplayMode displayMode;
}

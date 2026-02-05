package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ContentType;
import com.app.questofseoul.domain.enums.DisplayMode;
import com.app.questofseoul.domain.enums.Language;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "node_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NodeContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private QuestNode node;

    @Column(name = "content_order", nullable = false)
    private Integer contentOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "voice_style")
    private String voiceStyle;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_mode")
    private DisplayMode displayMode = DisplayMode.PARAGRAPH;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static NodeContent create(QuestNode node, Integer contentOrder, 
                                    ContentType contentType, Language language, 
                                    String body, String audioUrl, String voiceStyle, 
                                    DisplayMode displayMode) {
        NodeContent content = new NodeContent();
        content.node = node;
        content.contentOrder = contentOrder;
        content.contentType = contentType;
        content.language = language;
        content.body = body;
        content.audioUrl = audioUrl;
        content.voiceStyle = voiceStyle;
        content.displayMode = displayMode != null ? displayMode : DisplayMode.PARAGRAPH;
        return content;
    }

    public void setContentOrder(Integer contentOrder) { this.contentOrder = contentOrder; }
    public void setContentType(ContentType contentType) { this.contentType = contentType; }
    public void setLanguage(Language language) { this.language = language; }
    public void setBody(String body) { this.body = body; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public void setVoiceStyle(String voiceStyle) { this.voiceStyle = voiceStyle; }
    public void setDisplayMode(DisplayMode displayMode) { this.displayMode = displayMode != null ? displayMode : DisplayMode.PARAGRAPH; }
}

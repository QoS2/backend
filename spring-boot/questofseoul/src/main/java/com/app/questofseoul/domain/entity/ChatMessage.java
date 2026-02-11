package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ChatRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_content_id", nullable = false)
    private ChatContent chatContent;

    @Column(name = "msg_idx", nullable = false)
    private Integer msgIdx;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRole role = ChatRole.GUIDE;

    @Column(name = "text_en", columnDefinition = "TEXT", nullable = false)
    private String textEn;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_json", columnDefinition = "jsonb")
    private Map<String, Object> actionJson; // OPEN_STEP_PAGE, SHOW_IMAGE_GALLERY 등

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessageAsset> assets = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static ChatMessage create(ChatContent chatContent, Integer msgIdx, String textEn, Map<String, Object> actionJson) {
        ChatMessage m = new ChatMessage();
        m.chatContent = chatContent;
        m.msgIdx = msgIdx;
        m.textEn = textEn;
        m.actionJson = actionJson;
        return m;
    }
}

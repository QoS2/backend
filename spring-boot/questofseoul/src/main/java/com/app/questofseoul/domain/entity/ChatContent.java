package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ChatRefType;
import com.app.questofseoul.domain.enums.Language;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false)
    private ChatRefType refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language = Language.KO;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "chatContent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static ChatContent create(ChatRefType refType, Long refId, Language language) {
        ChatContent c = new ChatContent();
        c.refType = refType;
        c.refId = refId;
        c.language = language != null ? language : Language.KO;
        return c;
    }
}

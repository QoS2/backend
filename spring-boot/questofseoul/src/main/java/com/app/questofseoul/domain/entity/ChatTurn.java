package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ChatRole;
import com.app.questofseoul.domain.enums.ChatSource;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "chat_turns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(name = "turn_idx", nullable = false)
    private Integer turnIdx;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatSource source;

    @Column(columnDefinition = "TEXT")
    private String text;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_json", columnDefinition = "jsonb")
    private Map<String, Object> actionJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_json", columnDefinition = "jsonb")
    private Map<String, Object> metaJson; // assets 등

    @Column(name = "turn_key")
    private String turnKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static ChatTurn create(ChatSession session, Integer turnIdx, ChatRole role, ChatSource source,
                                  String text, Map<String, Object> actionJson, Map<String, Object> metaJson) {
        ChatTurn t = new ChatTurn();
        t.session = session;
        t.turnIdx = turnIdx;
        t.role = role;
        t.source = source;
        t.text = text;
        t.actionJson = actionJson;
        t.metaJson = metaJson;
        return t;
    }
}

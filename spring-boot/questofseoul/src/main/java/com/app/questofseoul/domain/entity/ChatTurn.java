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

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private ChatSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ChatRole role;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private SpotContentStep step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_line_id")
    private SpotScriptLine scriptLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_json", columnDefinition = "jsonb")
    private Map<String, Object> actionJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_json", columnDefinition = "jsonb")
    private Map<String, Object> contextJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static ChatTurn create(ChatSession session, ChatSource source, ChatRole role, String text) {
        ChatTurn t = new ChatTurn();
        t.session = session;
        t.source = source;
        t.role = role;
        t.text = text;
        return t;
    }

    public static ChatTurn createScript(ChatSession session, SpotContentStep step, SpotScriptLine scriptLine) {
        ChatTurn t = new ChatTurn();
        t.session = session;
        t.source = ChatSource.SCRIPT;
        t.role = ChatRole.GUIDE;
        t.text = scriptLine != null ? scriptLine.getText() : null;
        t.step = step;
        t.scriptLine = scriptLine;
        return t;
    }
}

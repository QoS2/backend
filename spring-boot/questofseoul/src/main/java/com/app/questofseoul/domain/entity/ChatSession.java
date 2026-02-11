package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ChatRefType;
import com.app.questofseoul.domain.enums.SessionKind;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_run_id", nullable = false)
    private TourRun tourRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_kind", nullable = false)
    private SessionKind sessionKind;

    @Enumerated(EnumType.STRING)
    @Column(name = "context_ref_type")
    private ChatRefType contextRefType;

    @Column(name = "context_ref_id")
    private Long contextRefId;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatTurn> turns = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }

    public static ChatSession create(User user, TourRun tourRun, SessionKind sessionKind,
                                     ChatRefType contextRefType, Long contextRefId) {
        ChatSession s = new ChatSession();
        s.user = user;
        s.tourRun = tourRun;
        s.sessionKind = sessionKind;
        s.contextRefType = contextRefType;
        s.contextRefId = contextRefId;
        return s;
    }
}

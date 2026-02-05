package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ActionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_quest_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQuestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private QuestNode node;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private NodeAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "user_input", columnDefinition = "TEXT")
    private String userInput;

    @Column(name = "photo_url")
    private String photoUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_option", columnDefinition = "jsonb")
    private Map<String, Object> selectedOption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static UserQuestHistory create(UUID userId, Quest quest, QuestNode node, 
                                         NodeAction action, ActionType actionType, 
                                         String userInput, String photoUrl, 
                                         Map<String, Object> selectedOption) {
        UserQuestHistory history = new UserQuestHistory();
        history.userId = userId;
        history.quest = quest;
        history.node = node;
        history.action = action;
        history.actionType = actionType;
        history.userInput = userInput;
        history.photoUrl = photoUrl;
        history.selectedOption = selectedOption;
        return history;
    }
}

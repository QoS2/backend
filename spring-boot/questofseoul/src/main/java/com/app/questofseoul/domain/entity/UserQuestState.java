package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.QuestStatus;
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
@Table(name = "user_quest_state", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "quest_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQuestState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_node_id")
    private QuestNode currentNode;

    @Column(name = "current_content_order")
    private Integer currentContentOrder = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestStatus status = QuestStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }

    public static UserQuestState create(UUID userId, Quest quest, QuestNode currentNode) {
        UserQuestState state = new UserQuestState();
        state.userId = userId;
        state.quest = quest;
        state.currentNode = currentNode;
        state.currentContentOrder = 1;
        return state;
    }

    public void updateCurrentNode(QuestNode node) {
        this.currentNode = node;
        this.currentContentOrder = 1;
    }

    public void updateContentOrder(Integer order) {
        this.currentContentOrder = order;
    }

    public void updateState(Map<String, Object> newState) {
        this.state = newState;
    }

    public void complete() {
        this.status = QuestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}

package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ActionType;
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
import java.util.UUID;

@Entity
@Table(name = "node_actions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NodeAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private QuestNode node;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> options;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "action", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionEffect> effects = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static NodeAction create(QuestNode node, ActionType actionType, 
                                   String prompt, Map<String, Object> options) {
        NodeAction action = new NodeAction();
        action.node = node;
        action.actionType = actionType;
        action.prompt = prompt;
        action.options = options;
        return action;
    }

    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setOptions(Map<String, Object> options) { this.options = options; }
}

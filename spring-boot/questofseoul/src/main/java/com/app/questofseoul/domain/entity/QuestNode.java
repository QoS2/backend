package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.NodeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "quest_nodes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false)
    private NodeType nodeType;

    @Column(nullable = false)
    private String title;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(columnDefinition = "geography(Point,4326)")
    private Point geo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unlock_condition", columnDefinition = "jsonb")
    private Map<String, Object> unlockCondition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NodeContent> contents = new ArrayList<>();

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NodeAction> actions = new ArrayList<>();

    @OneToMany(mappedBy = "fromNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NodeTransition> outgoingTransitions = new ArrayList<>();

    @OneToMany(mappedBy = "toNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NodeTransition> incomingTransitions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static QuestNode create(Quest quest, NodeType nodeType, String title,
                                  Integer orderIndex, Point geo,
                                  Map<String, Object> unlockCondition) {
        QuestNode node = new QuestNode();
        node.quest = quest;
        node.nodeType = nodeType;
        node.title = title;
        node.orderIndex = orderIndex;
        node.geo = geo;
        node.unlockCondition = unlockCondition;
        return node;
    }

    public void setNodeType(NodeType nodeType) { this.nodeType = nodeType; }
    public void setTitle(String title) { this.title = title; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public void setGeo(Point geo) { this.geo = geo; }
    public void setUnlockCondition(Map<String, Object> unlockCondition) { this.unlockCondition = unlockCondition; }
}

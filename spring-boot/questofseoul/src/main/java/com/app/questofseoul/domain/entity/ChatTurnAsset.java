package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.ChatTurnAssetUsage;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "chat_turn_assets", uniqueConstraints = @UniqueConstraint(columnNames = {"turn_id", "usage", "sort_order"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatTurnAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_id", nullable = false)
    private ChatTurn turn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private MediaAsset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage", nullable = false)
    private ChatTurnAssetUsage usage = ChatTurnAssetUsage.ATTACHMENT;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_json", columnDefinition = "jsonb")
    private Map<String, Object> metaJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static ChatTurnAsset create(ChatTurn turn, MediaAsset asset) {
        ChatTurnAsset cta = new ChatTurnAsset();
        cta.turn = turn;
        cta.asset = asset;
        return cta;
    }
}

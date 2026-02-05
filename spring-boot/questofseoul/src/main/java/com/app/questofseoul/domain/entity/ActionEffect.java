package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.EffectType;
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
@Table(name = "action_effects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActionEffect {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private NodeAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false)
    private EffectType effectType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "effect_value", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> effectValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static ActionEffect create(NodeAction action, EffectType effectType, 
                                     Map<String, Object> effectValue) {
        ActionEffect effect = new ActionEffect();
        effect.action = action;
        effect.effectType = effectType;
        effect.effectValue = effectValue;
        return effect;
    }

    public void setEffectType(EffectType effectType) { this.effectType = effectType; }
    public void setEffectValue(Map<String, Object> effectValue) { this.effectValue = effectValue; }
}

package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.MissionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "missions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false)
    private MissionType missionType;

    @Column(name = "prompt", nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json", columnDefinition = "jsonb")
    private Map<String, Object> optionsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer_json", columnDefinition = "jsonb")
    private Map<String, Object> answerJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_json", columnDefinition = "jsonb")
    private Map<String, Object> metaJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    public static Mission create(MissionType type, String prompt) {
        Mission m = new Mission();
        m.missionType = type;
        m.prompt = prompt;
        return m;
    }

    public void setPrompt(String prompt) { this.prompt = prompt; }
    public void setMissionType(MissionType type) { this.missionType = type; }
    public void setOptionsJson(java.util.Map<String, Object> optionsJson) { this.optionsJson = optionsJson; }
    public void setAnswerJson(java.util.Map<String, Object> answerJson) { this.answerJson = answerJson; }
    public void setMetaJson(java.util.Map<String, Object> metaJson) { this.metaJson = metaJson; }
}

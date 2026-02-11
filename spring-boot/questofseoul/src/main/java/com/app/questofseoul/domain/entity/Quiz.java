package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.QuizType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", unique = true)
    private String externalKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType type;

    @Column(name = "prompt_en", columnDefinition = "TEXT")
    private String promptEn;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spec_json", columnDefinition = "jsonb")
    private Map<String, Object> specJson; // options, blanks, photo URLs 등

    @Column(name = "answer_key_hash")
    private String answerKeyHash;

    @Column(name = "hint_en", columnDefinition = "TEXT")
    private String hintEn;

    @Column(name = "mint_reward")
    private Integer mintReward = 0;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Quiz create(Step step, String externalKey, QuizType type, String promptEn,
                              Map<String, Object> specJson, String hintEn, Integer mintReward) {
        Quiz q = new Quiz();
        q.step = step;
        q.externalKey = externalKey;
        q.type = type;
        q.promptEn = promptEn;
        q.specJson = specJson;
        q.hintEn = hintEn;
        q.mintReward = mintReward != null ? mintReward : 0;
        return q;
    }
}

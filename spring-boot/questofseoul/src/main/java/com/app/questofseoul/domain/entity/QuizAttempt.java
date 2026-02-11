package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "quiz_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private String status = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, FAILED

    @Column(nullable = false)
    private Integer attempts = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_answer_json", columnDefinition = "jsonb")
    private Map<String, Object> lastAnswerJson;

    @Column(name = "first_completed_at")
    private LocalDateTime firstCompletedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static QuizAttempt create(User user, Quiz quiz) {
        QuizAttempt qa = new QuizAttempt();
        qa.user = user;
        qa.quiz = quiz;
        qa.status = "IN_PROGRESS";
        qa.attempts = 0;
        return qa;
    }
}

package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.MissionAttemptStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "user_mission_attempts", uniqueConstraints = @UniqueConstraint(columnNames = {"tour_run_id", "step_id", "attempt_no"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMissionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_run_id", nullable = false)
    private TourRun tourRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private SpotContentStep step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MissionAttemptStatus status = MissionAttemptStatus.STARTED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer_json", columnDefinition = "jsonb")
    private Map<String, Object> answerJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "submission_assets_json", columnDefinition = "jsonb")
    private Object submissionAssetsJson;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "score")
    private Integer score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) startedAt = LocalDateTime.now();
    }

    public void setAttemptNo(Integer attemptNo) { this.attemptNo = attemptNo; }
    public void setAnswerJson(java.util.Map<String, Object> answerJson) { this.answerJson = answerJson; }
    public void setStatus(MissionAttemptStatus status) { this.status = status; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    public void setScore(Integer score) { this.score = score; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public static UserMissionAttempt create(TourRun run, SpotContentStep step, Mission mission) {
        UserMissionAttempt a = new UserMissionAttempt();
        a.tourRun = run;
        a.step = step;
        a.mission = mission;
        return a;
    }
}

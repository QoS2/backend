package com.app.questofseoul.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "spot_script_lines", uniqueConstraints = @UniqueConstraint(columnNames = {"step_id", "seq"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpotScriptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private SpotContentStep step;

    @Column(name = "seq", nullable = false)
    private Integer seq = 1;

    @Column(name = "role", nullable = false)
    private String role = "GUIDE";

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "scriptLine", cascade = CascadeType.ALL)
    private List<ScriptLineAsset> assets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static SpotScriptLine create(SpotContentStep step, int seq, String text) {
        SpotScriptLine sl = new SpotScriptLine();
        sl.step = step;
        sl.seq = seq;
        sl.text = text;
        return sl;
    }
}

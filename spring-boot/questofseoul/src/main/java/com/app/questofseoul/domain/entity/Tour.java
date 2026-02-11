package com.app.questofseoul.domain.entity;

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

@Entity
@Table(name = "tours")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", unique = true)
    private String externalKey;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "info_json", columnDefinition = "jsonb")
    private Map<String, Object> infoJson; // 입장료, 이용시간 등

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "good_to_know_json", columnDefinition = "jsonb")
    private Map<String, Object> goodToKnowJson;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Step> steps = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Waypoint> waypoints = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoSpot> photoSpots = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Treasure> treasures = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Tour create(String externalKey, String titleEn, String descriptionEn,
                              Map<String, Object> infoJson, Map<String, Object> goodToKnowJson) {
        Tour t = new Tour();
        t.externalKey = externalKey;
        t.titleEn = titleEn;
        t.descriptionEn = descriptionEn;
        t.infoJson = infoJson;
        t.goodToKnowJson = goodToKnowJson;
        return t;
    }

    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public void setInfoJson(Map<String, Object> infoJson) { this.infoJson = infoJson; }
    public void setGoodToKnowJson(Map<String, Object> goodToKnowJson) { this.goodToKnowJson = goodToKnowJson; }
}

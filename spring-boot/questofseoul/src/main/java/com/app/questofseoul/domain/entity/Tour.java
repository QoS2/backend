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

    @Column(name = "title")
    private String title;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "info_json", columnDefinition = "jsonb")
    private Map<String, Object> infoJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "good_to_know_json", columnDefinition = "jsonb")
    private Map<String, Object> goodToKnowJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_spot_id")
    private TourSpot startSpot;

    @Column(name = "is_published")
    private Boolean isPublished = true;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
    private List<TourSpot> spots = new ArrayList<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
    private List<TourTag> tourTags = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public String getDisplayTitle() {
        return title != null && !title.isBlank() ? title : titleEn;
    }

    public String getDisplayDescription() {
        return description != null && !description.isBlank() ? description : descriptionEn;
    }

    public void setTitle(String title) { this.title = title; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }
    public void setDescription(String description) { this.description = description; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public void setInfoJson(Map<String, Object> infoJson) { this.infoJson = infoJson; }
    public void setGoodToKnowJson(Map<String, Object> goodToKnowJson) { this.goodToKnowJson = goodToKnowJson; }
    public void setStartSpot(TourSpot startSpot) { this.startSpot = startSpot; }

    public static Tour create(String externalKey, String titleEn, String descriptionEn,
                             Map<String, Object> infoJson, Map<String, Object> goodToKnowJson) {
        Tour t = new Tour();
        t.externalKey = externalKey;
        t.titleEn = titleEn;
        t.descriptionEn = descriptionEn;
        t.infoJson = infoJson != null ? infoJson : Map.of();
        t.goodToKnowJson = goodToKnowJson != null ? goodToKnowJson : Map.of();
        return t;
    }
}

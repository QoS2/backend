package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.SpotType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tour_spots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TourSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SpotType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_spot_id")
    private TourSpot parentSpot;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "title_kr")
    private String titleKr;

    @Column(name = "pronunciation_url", columnDefinition = "TEXT")
    private String pronunciationUrl;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "radius_m", nullable = false)
    private Integer radiusM = 50;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @Column(name = "ai_chat_enabled", nullable = false)
    private Boolean aiChatEnabled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "parentSpot", cascade = CascadeType.ALL)
    private List<TourSpot> childSpots = new ArrayList<>();

    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpotContentStep> contentSteps = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setDescription(String description) { this.description = description; }
    public void setTitle(String title) { this.title = title; }
    public void setTitleKr(String titleKr) { this.titleKr = titleKr; }
    public void setPronunciationUrl(String pronunciationUrl) { this.pronunciationUrl = pronunciationUrl; }
    public void setAddress(String address) { this.address = address; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public void setRadiusM(Integer radiusM) { this.radiusM = radiusM != null ? radiusM : 50; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public static TourSpot create(Tour tour, SpotType type, String title, Double lat, Double lng, Integer orderIndex) {
        TourSpot s = new TourSpot();
        s.tour = tour;
        s.type = type;
        s.title = title;
        s.latitude = lat;
        s.longitude = lng;
        s.orderIndex = orderIndex != null ? orderIndex : 0;
        return s;
    }
}

package com.app.questofseoul.dto.tour;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TourDetailResponse {

    private Long tourId;
    private String title;
    private String description;
    private List<TagDto> tags;
    private CountsDto counts;
    private InfoDto info;
    private List<String> goodToKnow;
    private StartSpotDto startSpot;
    private List<MapSpotDto> mapSpots;
    private AccessDto access;
    private CurrentRunDto currentRun;
    private ActionsDto actions;
    private List<MainMissionPathItemDto> mainMissionPath;
    /** 투어 디테일 캐러셀용 썸네일 URL 목록 (메인 플레이스 이미지 기반) */
    private List<String> thumbnails;

    @Data
    @Builder
    public static class TagDto {
        private Long id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    public static class CountsDto {
        private int main;
        private int sub;
        private int photo;
        private int treasure;
        private int missions;
    }

    @Data
    @Builder
    public static class InfoDto {
        private Map<String, Object> entrance_fee;
        private List<Map<String, Object>> available_hours;
        private Integer estimated_duration_min;
    }

    @Data
    @Builder
    public static class StartSpotDto {
        private Long spotId;
        private String title;
        private Double lat;
        private Double lng;
        private Integer radiusM;
    }

    @Data
    @Builder
    public static class MapSpotDto {
        private Long spotId;
        private String type;
        private String title;
        private Double lat;
        private Double lng;
        private String thumbnailUrl;
        private Boolean isHighlight;
    }

    @Data
    @Builder
    public static class AccessDto {
        private String status;
        private boolean hasAccess;
    }

    @Data
    @Builder
    public static class CurrentRunDto {
        private Long runId;
        private String status;
        private String startedAt;
        private ProgressDto progress;
    }

    @Data
    @Builder
    public static class ProgressDto {
        private int completedCount;
        private int totalCount;
        private List<Long> completedSpotIds;
    }

    @Data
    @Builder
    public static class ActionsDto {
        private String primaryButton;
        private String secondaryButton;
        private List<String> moreActions;
    }

    @Data
    @Builder
    public static class MainMissionPathItemDto {
        private Long spotId;
        private String spotTitle;
        private int orderIndex;
        private List<MissionItemDto> missions;
    }

    @Data
    @Builder
    public static class MissionItemDto {
        private Long stepId;
        private Long missionId;
        private String title;
    }

}

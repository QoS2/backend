package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Mission;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.Tour;
import com.app.questofseoul.domain.entity.TourRun;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.entity.User;
import com.app.questofseoul.domain.entity.UserMissionAttempt;
import com.app.questofseoul.domain.entity.UserSpotProgress;
import com.app.questofseoul.domain.enums.MissionType;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.dto.tour.MissionSubmitRequest;
import com.app.questofseoul.dto.tour.MissionSubmitResponse;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.TourRunRepository;
import com.app.questofseoul.repository.UserMissionAttemptRepository;
import com.app.questofseoul.repository.UserSpotProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MissionServiceTest {

    @Mock private TourRunRepository tourRunRepository;
    @Mock private SpotContentStepRepository spotContentStepRepository;
    @Mock private UserMissionAttemptRepository userMissionAttemptRepository;
    @Mock private UserSpotProgressRepository userSpotProgressRepository;

    @Mock private TourRun run;
    @Mock private User user;
    @Mock private Tour tour;
    @Mock private TourSpot spot;
    @Mock private SpotContentStep currentStep;
    @Mock private SpotContentStep nextStep;
    @Mock private Mission mission;
    @Mock private UserSpotProgress progress;

    private MissionService missionService;
    private final UUID userId = UUID.randomUUID();
    private final Long runId = 10L;
    private final Long stepId = 100L;
    private final Long spotId = 5L;

    @BeforeEach
    void setUp() {
        missionService = new MissionService(
                tourRunRepository,
                spotContentStepRepository,
                userMissionAttemptRepository,
                userSpotProgressRepository
        );

        when(tourRunRepository.findById(runId)).thenReturn(Optional.of(run));
        when(run.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(run.getTour()).thenReturn(tour);
        when(tour.getId()).thenReturn(1L);

        when(spotContentStepRepository.findById(stepId)).thenReturn(Optional.of(currentStep));
        when(currentStep.getSpot()).thenReturn(spot);
        when(spot.getId()).thenReturn(spotId);
        when(spot.getTour()).thenReturn(tour);
        when(currentStep.getMission()).thenReturn(mission);
        when(currentStep.getLanguage()).thenReturn("ko");
        when(currentStep.getId()).thenReturn(stepId);

        when(mission.getMissionType()).thenReturn(MissionType.QUIZ);
        when(mission.getAnswerJson()).thenReturn(Map.of("answer", "a"));

        when(userMissionAttemptRepository.findByTourRun_IdAndStep_Id(runId, stepId)).thenReturn(List.of());
        when(userMissionAttemptRepository.save(any(UserMissionAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userSpotProgressRepository.findByTourRunIdAndSpotId(runId, spotId))
                .thenReturn(Optional.of(progress));
    }

    @Test
    void submitMission_returnsNextMissionApi_whenMissionStepRemains() {
        when(nextStep.getId()).thenReturn(101L);
        when(spotContentStepRepository.findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(
                eq(spotId), eq(StepKind.MISSION), eq("ko")
        )).thenReturn(List.of(currentStep, nextStep));

        MissionSubmitResponse response = missionService.submitMission(
                userId,
                runId,
                stepId,
                new MissionSubmitRequest(MissionType.QUIZ, null, null, "a")
        );

        assertEquals("/api/v1/content-steps/101/mission", response.nextStepApi());
        verify(progress).unlock();
        verify(progress, never()).complete();
    }

    @Test
    void submitMission_returnsNextSpotApi_whenCurrentMissionIsLast() {
        when(spotContentStepRepository.findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(
                eq(spotId), eq(StepKind.MISSION), eq("ko")
        )).thenReturn(List.of(currentStep));

        MissionSubmitResponse response = missionService.submitMission(
                userId,
                runId,
                stepId,
                new MissionSubmitRequest(MissionType.QUIZ, null, null, "a")
        );

        assertEquals("/api/v1/tour-runs/10/next-spot", response.nextStepApi());
        verify(progress).complete();
    }

    @Test
    void submitMission_throwsWhenRequestTypeDiffersFromMissionType() {
        when(mission.getMissionType()).thenReturn(MissionType.PHOTO);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> missionService.submitMission(
                        userId,
                        runId,
                        stepId,
                        new MissionSubmitRequest(MissionType.QUIZ, null, null, "a")
                )
        );

        assertEquals(
                "missionType mismatch: expected PHOTO, got QUIZ",
                ex.getMessage()
        );
    }

    @Test
    void submitMission_persistsSelectedOptionIdField() {
        when(spotContentStepRepository.findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(
                eq(spotId), eq(StepKind.MISSION), eq("ko")
        )).thenReturn(List.of(currentStep));

        missionService.submitMission(
                userId,
                runId,
                stepId,
                new MissionSubmitRequest(MissionType.QUIZ, null, null, "b")
        );

        ArgumentCaptor<UserMissionAttempt> captor = ArgumentCaptor.forClass(UserMissionAttempt.class);
        verify(userMissionAttemptRepository).save(captor.capture());
        assertEquals("b", captor.getValue().getAnswerJson().get("selectedOptionId"));
    }
}

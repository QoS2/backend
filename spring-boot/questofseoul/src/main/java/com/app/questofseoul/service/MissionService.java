package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Mission;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.TourRun;
import com.app.questofseoul.domain.entity.UserSpotProgress;
import com.app.questofseoul.domain.entity.UserMissionAttempt;
import com.app.questofseoul.domain.enums.MissionAttemptStatus;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.dto.tour.MissionSubmitRequest;
import com.app.questofseoul.dto.tour.MissionSubmitResponse;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.TourRunRepository;
import com.app.questofseoul.repository.UserMissionAttemptRepository;
import com.app.questofseoul.repository.UserSpotProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final TourRunRepository tourRunRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final UserMissionAttemptRepository userMissionAttemptRepository;
    private final UserSpotProgressRepository userSpotProgressRepository;

    @Transactional
    public MissionSubmitResponse submitMission(java.util.UUID userId, Long runId, Long stepId, MissionSubmitRequest request) {
        TourRun run = tourRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }

        SpotContentStep step = spotContentStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (!step.getSpot().getTour().getId().equals(run.getTour().getId())) {
            throw new AuthorizationException("Step is not in your current tour run");
        }
        if (step.getMission() == null) {
            throw new ResourceNotFoundException("Step has no mission");
        }
        Mission mission = step.getMission();
        if (request.missionType() != mission.getMissionType()) {
            throw new IllegalArgumentException(
                    "missionType mismatch: expected " + mission.getMissionType().name() + ", got " + request.missionType().name()
            );
        }

        int attemptNo = (int) userMissionAttemptRepository.findByTourRun_IdAndStep_Id(runId, stepId).stream().count() + 1;
        UserMissionAttempt attempt = UserMissionAttempt.create(run, step, mission);
        attempt.setAttemptNo(attemptNo);

        Map<String, Object> answerJson = new HashMap<>();
        if (request.missionType() != null) answerJson.put("missionType", request.missionType().name());
        if (request.userInput() != null) answerJson.put("userInput", request.userInput());
        if (request.photoUrl() != null) answerJson.put("photoUrl", request.photoUrl());
        if (request.selectedOptionId() != null) answerJson.put("selectedOptionId", request.selectedOptionId());
        attempt.setAnswerJson(answerJson);
        attempt.setStatus(MissionAttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());

        // 채점
        boolean isCorrect = gradeMission(mission, answerJson);
        attempt.setIsCorrect(isCorrect);
        attempt.setScore(isCorrect ? 10 : 0);
        attempt.setFeedback(isCorrect ? "정답입니다!" : "다시 생각해 보세요.");
        attempt.setGradedAt(LocalDateTime.now());
        attempt.setStatus(MissionAttemptStatus.GRADED);

        attempt = userMissionAttemptRepository.save(attempt);

        Long nextMissionStepId = findNextMissionStepId(step);

        // 미션 제출 시 스팟 진행 상태 업데이트
        UserSpotProgress progress = userSpotProgressRepository.findByTourRunIdAndSpotId(runId, step.getSpot().getId())
                .orElseGet(() -> userSpotProgressRepository.save(UserSpotProgress.create(run, step.getSpot())));
        if (nextMissionStepId == null) {
            progress.complete();
        } else {
            progress.unlock();
        }

        // nextStepApi 계산
        String nextStepApi = nextMissionStepId != null
                ? "/api/v1/content-steps/" + nextMissionStepId + "/mission"
                : "/api/v1/tour-runs/" + runId + "/next-spot";

        return new MissionSubmitResponse(
                attempt.getId(), isCorrect, attempt.getScore(), attempt.getFeedback(), nextStepApi);
    }

    private boolean gradeMission(Mission mission, Map<String, Object> answerJson) {
        if (mission.getAnswerJson() == null || mission.getAnswerJson().isEmpty()) {
            return true;
        }
        Object userAnswer = answerJson.get("userInput");
        if (userAnswer == null) userAnswer = answerJson.get("selectedOptionId");
        Object correctAnswer = mission.getAnswerJson().get("answer");
        if (correctAnswer == null) correctAnswer = mission.getAnswerJson().get("value");
        if (correctAnswer == null) return true;
        return correctAnswer.toString().equalsIgnoreCase(userAnswer != null ? userAnswer.toString() : "");
    }

    private Long findNextMissionStepId(SpotContentStep currentStep) {
        List<SpotContentStep> missionSteps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(
                        currentStep.getSpot().getId(),
                        StepKind.MISSION,
                        currentStep.getLanguage()
                );
        for (int i = 0; i < missionSteps.size(); i++) {
            if (!missionSteps.get(i).getId().equals(currentStep.getId())) {
                continue;
            }
            if (i + 1 < missionSteps.size()) {
                return missionSteps.get(i + 1).getId();
            }
            return null;
        }
        return null;
    }
}

package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Mission;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.TourRun;
import com.app.questofseoul.domain.entity.UserMissionAttempt;
import com.app.questofseoul.domain.enums.MissionAttemptStatus;
import com.app.questofseoul.dto.tour.MissionSubmitRequest;
import com.app.questofseoul.dto.tour.MissionSubmitResponse;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.TourRunRepository;
import com.app.questofseoul.repository.UserMissionAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final TourRunRepository tourRunRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final UserMissionAttemptRepository userMissionAttemptRepository;

    @Transactional
    public MissionSubmitResponse submitMission(java.util.UUID userId, Long runId, Long stepId, MissionSubmitRequest request) {
        TourRun run = tourRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Not your tour run");
        }

        SpotContentStep step = spotContentStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (step.getMission() == null) {
            throw new ResourceNotFoundException("Step has no mission");
        }
        Mission mission = step.getMission();

        int attemptNo = (int) userMissionAttemptRepository.findByTourRun_IdAndStep_Id(runId, stepId).stream().count() + 1;
        UserMissionAttempt attempt = UserMissionAttempt.create(run, step, mission);
        attempt.setAttemptNo(attemptNo);

        Map<String, Object> answerJson = new HashMap<>();
        if (request.userInput() != null) answerJson.put("userInput", request.userInput());
        if (request.photoUrl() != null) answerJson.put("photoUrl", request.photoUrl());
        if (request.selectedOption() != null) answerJson.put("selectedOption", request.selectedOption());
        attempt.setAnswerJson(answerJson);
        attempt.setStatus(MissionAttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());

        // 간단 채점 (QUIZ: answer_json과 비교, 그 외는 success=true)
        boolean isCorrect = gradeMission(mission, answerJson);
        attempt.setIsCorrect(isCorrect);
        attempt.setScore(isCorrect ? 10 : 0);
        attempt.setFeedback(isCorrect ? "정답입니다!" : "다시 생각해 보세요.");
        attempt.setGradedAt(LocalDateTime.now());
        attempt.setStatus(MissionAttemptStatus.GRADED);

        attempt = userMissionAttemptRepository.save(attempt);

        return new MissionSubmitResponse(
                attempt.getId(), true, isCorrect, attempt.getScore(), attempt.getFeedback());
    }

    private boolean gradeMission(Mission mission, Map<String, Object> answerJson) {
        if (mission.getAnswerJson() == null || mission.getAnswerJson().isEmpty()) {
            return true; // 답 없으면 통과
        }
        Object userAnswer = answerJson.get("userInput");
        if (userAnswer == null) userAnswer = answerJson.get("selectedOption");
        Object correctAnswer = mission.getAnswerJson().get("answer");
        if (correctAnswer == null) correctAnswer = mission.getAnswerJson().get("value");
        if (correctAnswer == null) return true;
        return correctAnswer.toString().equalsIgnoreCase(userAnswer != null ? userAnswer.toString() : "");
    }
}

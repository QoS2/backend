package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Mission;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.UserMissionAttempt;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.dto.tour.MissionStepDetailResponse;
import com.app.questofseoul.exception.AuthorizationException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.TourRunRepository;
import com.app.questofseoul.repository.UserMissionAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MissionStepService {

    private final SpotContentStepRepository spotContentStepRepository;
    private final TourRunRepository tourRunRepository;
    private final UserMissionAttemptRepository userMissionAttemptRepository;

    /** MISSION 스텝 상세 조회 (prompt, optionsJson 등) */
    @Transactional(readOnly = true)
    public MissionStepDetailResponse getMissionStepDetail(Long stepId, Long runId, UUID userId) {
        SpotContentStep step = spotContentStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (step.getKind() != StepKind.MISSION) {
            throw new ResourceNotFoundException("Step is not a mission step");
        }
        Mission mission = step.getMission();
        if (mission == null) {
            throw new ResourceNotFoundException("Step has no mission");
        }

        Boolean isCompleted = false;
        String selectedOptionId = null;
        Map<String, Object> answerJson = Map.of();

        if (runId != null) {
            if (userId == null) {
                throw new AuthorizationException("인증된 사용자 정보가 필요합니다");
            }
            tourRunRepository.findByIdAndUserId(runId, userId)
                    .orElseThrow(() -> new AuthorizationException("Not your tour run"));

            UserMissionAttempt latestAttempt = userMissionAttemptRepository
                    .findTopByTourRun_IdAndStep_IdOrderByAttemptNoDesc(runId, stepId)
                    .orElse(null);
            if (latestAttempt != null) {
                answerJson = latestAttempt.getAnswerJson() != null ? latestAttempt.getAnswerJson() : Map.of();
                Object selected = answerJson.get("selectedOptionId");
                selectedOptionId = selected != null ? String.valueOf(selected) : null;
                isCompleted = Boolean.TRUE.equals(latestAttempt.getIsCorrect());
            }
        }

        return new MissionStepDetailResponse(
                step.getId(),
                mission.getId(),
                mission.getMissionType().name(),
                mission.getPrompt(),
                mission.getOptionsJson() != null ? mission.getOptionsJson() : java.util.Map.of(),
                step.getTitle() != null ? step.getTitle() : "Mission",
                isCompleted,
                selectedOptionId,
                answerJson
        );
    }
}

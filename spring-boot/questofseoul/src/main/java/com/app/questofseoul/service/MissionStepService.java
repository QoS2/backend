package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.Mission;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.dto.tour.MissionStepDetailResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.SpotContentStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionStepService {

    private final SpotContentStepRepository spotContentStepRepository;

    /** MISSION 스텝 상세 조회 (prompt, optionsJson 등) */
    @Transactional(readOnly = true)
    public MissionStepDetailResponse getMissionStepDetail(Long stepId) {
        SpotContentStep step = spotContentStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (step.getKind() != StepKind.MISSION) {
            throw new ResourceNotFoundException("Step is not a mission step");
        }
        Mission mission = step.getMission();
        if (mission == null) {
            throw new ResourceNotFoundException("Step has no mission");
        }
        return new MissionStepDetailResponse(
                step.getId(),
                mission.getId(),
                mission.getMissionType().name(),
                mission.getPrompt(),
                mission.getOptionsJson() != null ? mission.getOptionsJson() : java.util.Map.of(),
                step.getTitle() != null ? step.getTitle() : "Mission"
        );
    }
}

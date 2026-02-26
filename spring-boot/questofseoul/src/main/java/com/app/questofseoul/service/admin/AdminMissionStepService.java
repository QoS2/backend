package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.Mission;
import com.app.questofseoul.domain.entity.SpotContentStep;
import com.app.questofseoul.domain.entity.TourSpot;
import com.app.questofseoul.domain.enums.MissionType;
import com.app.questofseoul.domain.enums.StepKind;
import com.app.questofseoul.domain.enums.StepNextAction;
import com.app.questofseoul.dto.admin.MissionStepCreateRequest;
import com.app.questofseoul.dto.admin.MissionStepResponse;
import com.app.questofseoul.dto.admin.MissionStepUpdateRequest;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.MissionRepository;
import com.app.questofseoul.repository.SpotContentStepRepository;
import com.app.questofseoul.repository.TourSpotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMissionStepService {

    private final TourSpotRepository tourSpotRepository;
    private final SpotContentStepRepository spotContentStepRepository;
    private final MissionRepository missionRepository;

    @Transactional(readOnly = true)
    public List<MissionStepResponse> list(Long tourId, Long spotId) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));
        List<SpotContentStep> steps = spotContentStepRepository
                .findBySpot_IdAndKindAndLanguageOrderByStepIndexAsc(spot.getId(), StepKind.MISSION, "ko");
        return steps.stream()
                .map(s -> {
                    Mission m = s.getMission();
                    return new MissionStepResponse(
                            s.getId(),
                            m != null ? m.getId() : null,
                            m != null ? m.getMissionType().name() : null,
                            m != null ? m.getPrompt() : null,
                            m != null && m.getOptionsJson() != null ? m.getOptionsJson() : Map.of(),
                            m != null && m.getAnswerJson() != null ? m.getAnswerJson() : Map.of(),
                            s.getTitle(),
                            s.getStepIndex()
                    );
                })
                .toList();
    }

    @Transactional
    public MissionStepResponse create(Long tourId, Long spotId, MissionStepCreateRequest req) {
        TourSpot spot = tourSpotRepository.findByIdAndTourId(spotId, tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Spot not found"));

        MissionType type = req.missionType();
        Mission mission = Mission.create(type, req.prompt() != null ? req.prompt() : "");
        if (req.optionsJson() != null && !req.optionsJson().isEmpty()) {
            mission.setOptionsJson(req.optionsJson());
        }
        if (req.answerJson() != null && !req.answerJson().isEmpty()) {
            mission.setAnswerJson(req.answerJson());
        }
        mission = missionRepository.save(mission);

        int nextIndex = spotContentStepRepository
                .findAllBySpotIdAndLanguageOrderByStepIndexAscIncludingUnpublished(spot.getId(), "ko")
                .stream()
                .mapToInt(SpotContentStep::getStepIndex)
                .max()
                .orElse(-1) + 1;

        SpotContentStep step = SpotContentStep.create(spot, StepKind.MISSION, nextIndex);
        step.setMission(mission);
        step.setTitle(req.title() != null && !req.title().isBlank() ? req.title() : "Mission");
        step = spotContentStepRepository.save(step);

        return new MissionStepResponse(
                step.getId(),
                mission.getId(),
                mission.getMissionType().name(),
                mission.getPrompt(),
                mission.getOptionsJson() != null ? mission.getOptionsJson() : Map.of(),
                mission.getAnswerJson() != null ? mission.getAnswerJson() : Map.of(),
                step.getTitle(),
                step.getStepIndex()
        );
    }

    @Transactional
    public MissionStepResponse update(Long tourId, Long spotId, Long stepId, MissionStepUpdateRequest req) {
        SpotContentStep step = spotContentStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (!step.getSpot().getTour().getId().equals(tourId) || !step.getSpot().getId().equals(spotId)) {
            throw new ResourceNotFoundException("Step not found");
        }
        if (step.getKind() != StepKind.MISSION) {
            throw new ResourceNotFoundException("Step is not a mission step");
        }
        if (Boolean.FALSE.equals(step.getIsPublished())) {
            throw new ResourceNotFoundException("Step not found");
        }
        Mission mission = step.getMission();
        if (mission == null) {
            throw new ResourceNotFoundException("Step has no mission");
        }

        if (req.prompt() != null) mission.setPrompt(req.prompt());
        if (req.optionsJson() != null) mission.setOptionsJson(req.optionsJson());
        if (req.answerJson() != null) mission.setAnswerJson(req.answerJson());
        if (req.title() != null) step.setTitle(req.title());

        missionRepository.save(mission);
        spotContentStepRepository.save(step);

        return new MissionStepResponse(
                step.getId(),
                mission.getId(),
                mission.getMissionType().name(),
                mission.getPrompt(),
                mission.getOptionsJson() != null ? mission.getOptionsJson() : Map.of(),
                mission.getAnswerJson() != null ? mission.getAnswerJson() : Map.of(),
                step.getTitle(),
                step.getStepIndex()
        );
    }

    @Transactional
    public void delete(Long tourId, Long spotId, Long stepId) {
        SpotContentStep step = spotContentStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        if (!step.getSpot().getTour().getId().equals(tourId) || !step.getSpot().getId().equals(spotId)) {
            throw new ResourceNotFoundException("Step not found");
        }
        if (step.getKind() != StepKind.MISSION) {
            throw new ResourceNotFoundException("Step is not a mission step");
        }
        if (Boolean.FALSE.equals(step.getIsPublished())) {
            throw new ResourceNotFoundException("Step not found");
        }
        Mission mission = step.getMission();
        // user_mission_attempts / chat_turns 가 step_id, mission_id를 참조할 수 있어 하드 삭제 대신 비공개 처리
        step.unpublish();
        step.setNextAction(null);
        spotContentStepRepository.save(step);
        if (mission != null) {
            List<SpotContentStep> linkedGuideSteps = spotContentStepRepository
                    .findBySpot_IdAndKindAndMission_Id(spotId, StepKind.GUIDE, mission.getId());
            linkedGuideSteps.forEach(linkedStep -> {
                linkedStep.setMission(null);
                if (linkedStep.getNextAction() == StepNextAction.MISSION_CHOICE) {
                    linkedStep.setNextAction(StepNextAction.NEXT);
                }
            });
            spotContentStepRepository.saveAll(linkedGuideSteps);
        }
    }
}

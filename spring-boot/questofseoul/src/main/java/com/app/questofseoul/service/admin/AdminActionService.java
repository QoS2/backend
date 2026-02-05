package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.NodeAction;
import com.app.questofseoul.domain.entity.QuestNode;
import com.app.questofseoul.dto.admin.ActionCreateRequest;
import com.app.questofseoul.dto.admin.ActionResponse;
import com.app.questofseoul.dto.admin.ActionUpdateRequest;
import com.app.questofseoul.dto.admin.EffectResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.NodeActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminActionService {

    private final NodeActionRepository actionRepository;
    private final AdminNodeService adminNodeService;

    @Transactional(readOnly = true)
    public List<ActionResponse> listByNode(UUID questId, UUID nodeId) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        return actionRepository.findByNodeId(nodeId).stream()
            .map(this::toActionResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ActionResponse get(UUID questId, UUID nodeId, UUID actionId, boolean includeEffects) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        NodeAction action = actionRepository.findById(actionId)
            .orElseThrow(() -> new ResourceNotFoundException("액션", actionId));
        if (!action.getNode().getId().equals(nodeId)) {
            throw new ResourceNotFoundException("액션", actionId);
        }
        return toActionResponse(action, includeEffects);
    }

    @Transactional
    public ActionResponse create(UUID questId, UUID nodeId, ActionCreateRequest req) {
        QuestNode node = adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        NodeAction action = NodeAction.create(
            node,
            req.getActionType(),
            req.getPrompt(),
            req.getOptions()
        );
        action = actionRepository.save(action);
        return toActionResponse(action);
    }

    @Transactional
    public ActionResponse update(UUID questId, UUID nodeId, UUID actionId, ActionUpdateRequest req) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        NodeAction action = actionRepository.findById(actionId)
            .orElseThrow(() -> new ResourceNotFoundException("액션", actionId));
        if (!action.getNode().getId().equals(nodeId)) {
            throw new ResourceNotFoundException("액션", actionId);
        }
        if (req.getActionType() != null) action.setActionType(req.getActionType());
        if (req.getPrompt() != null) action.setPrompt(req.getPrompt());
        if (req.getOptions() != null) action.setOptions(req.getOptions());
        action = actionRepository.save(action);
        return toActionResponse(action);
    }

    @Transactional
    public void delete(UUID questId, UUID nodeId, UUID actionId) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        NodeAction action = actionRepository.findById(actionId)
            .orElseThrow(() -> new ResourceNotFoundException("액션", actionId));
        if (!action.getNode().getId().equals(nodeId)) {
            throw new ResourceNotFoundException("액션", actionId);
        }
        actionRepository.delete(action);
    }

    private ActionResponse toActionResponse(NodeAction a) {
        return toActionResponse(a, false);
    }

    private ActionResponse toActionResponse(NodeAction a, boolean includeEffects) {
        var builder = ActionResponse.builder()
            .id(a.getId())
            .nodeId(a.getNode().getId())
            .actionType(a.getActionType())
            .prompt(a.getPrompt())
            .options(a.getOptions())
            .createdAt(a.getCreatedAt());
        if (includeEffects) {
            builder.effects(a.getEffects().stream()
                .map(e -> EffectResponse.builder()
                    .id(e.getId())
                    .actionId(e.getAction().getId())
                    .effectType(e.getEffectType())
                    .effectValue(e.getEffectValue())
                    .createdAt(e.getCreatedAt())
                    .build())
                .collect(Collectors.toList()));
        }
        return builder.build();
    }
}

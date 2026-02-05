package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.ActionEffect;
import com.app.questofseoul.domain.entity.NodeAction;
import com.app.questofseoul.dto.admin.EffectCreateRequest;
import com.app.questofseoul.dto.admin.EffectResponse;
import com.app.questofseoul.dto.admin.EffectUpdateRequest;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.ActionEffectRepository;
import com.app.questofseoul.repository.NodeActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEffectService {

    private final ActionEffectRepository effectRepository;
    private final NodeActionRepository actionRepository;
    private final AdminNodeService adminNodeService;

    @Transactional(readOnly = true)
    public List<EffectResponse> listByAction(UUID questId, UUID nodeId, UUID actionId) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        if (!actionRepository.existsByNodeIdAndId(nodeId, actionId)) {
            throw new ResourceNotFoundException("액션", actionId);
        }
        return effectRepository.findByActionId(actionId).stream()
            .map(this::toEffectResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EffectResponse get(UUID questId, UUID nodeId, UUID actionId, UUID effectId) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        if (!actionRepository.existsByNodeIdAndId(nodeId, actionId)) {
            throw new ResourceNotFoundException("액션", actionId);
        }
        ActionEffect effect = effectRepository.findById(effectId)
            .orElseThrow(() -> new ResourceNotFoundException("이펙트", effectId));
        if (!effect.getAction().getId().equals(actionId)) {
            throw new ResourceNotFoundException("이펙트", effectId);
        }
        return toEffectResponse(effect);
    }

    @Transactional
    public EffectResponse create(UUID questId, UUID nodeId, UUID actionId, EffectCreateRequest req) {
        NodeAction action = actionRepository.findById(actionId)
            .orElseThrow(() -> new ResourceNotFoundException("액션", actionId));
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        if (!action.getNode().getId().equals(nodeId)) {
            throw new ResourceNotFoundException("액션", actionId);
        }
        ActionEffect effect = ActionEffect.create(action, req.getEffectType(), req.getEffectValue());
        effect = effectRepository.save(effect);
        return toEffectResponse(effect);
    }

    @Transactional
    public EffectResponse update(UUID questId, UUID nodeId, UUID actionId, UUID effectId, EffectUpdateRequest req) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        if (!actionRepository.existsByNodeIdAndId(nodeId, actionId)) {
            throw new ResourceNotFoundException("액션", actionId);
        }
        ActionEffect effect = effectRepository.findById(effectId)
            .orElseThrow(() -> new ResourceNotFoundException("이펙트", effectId));
        if (!effect.getAction().getId().equals(actionId)) {
            throw new ResourceNotFoundException("이펙트", effectId);
        }
        if (req.getEffectType() != null) effect.setEffectType(req.getEffectType());
        if (req.getEffectValue() != null) effect.setEffectValue(req.getEffectValue());
        effect = effectRepository.save(effect);
        return toEffectResponse(effect);
    }

    @Transactional
    public void delete(UUID questId, UUID nodeId, UUID actionId, UUID effectId) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        if (!effectRepository.existsByActionIdAndId(actionId, effectId)) {
            throw new ResourceNotFoundException("이펙트", effectId);
        }
        ActionEffect effect = effectRepository.findById(effectId)
            .orElseThrow(() -> new ResourceNotFoundException("이펙트", effectId));
        effectRepository.delete(effect);
    }

    private EffectResponse toEffectResponse(ActionEffect e) {
        return EffectResponse.builder()
            .id(e.getId())
            .actionId(e.getAction().getId())
            .effectType(e.getEffectType())
            .effectValue(e.getEffectValue())
            .createdAt(e.getCreatedAt())
            .build();
    }
}

package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.NodeTransition;
import com.app.questofseoul.domain.entity.QuestNode;
import com.app.questofseoul.dto.admin.TransitionCreateRequest;
import com.app.questofseoul.dto.admin.TransitionResponse;
import com.app.questofseoul.dto.admin.TransitionUpdateRequest;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.exception.ValidationException;
import com.app.questofseoul.repository.NodeTransitionRepository;
import com.app.questofseoul.repository.QuestNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTransitionService {

    private final NodeTransitionRepository transitionRepository;
    private final QuestNodeRepository nodeRepository;
    private final AdminQuestService adminQuestService;

    @Transactional(readOnly = true)
    public List<TransitionResponse> listByQuest(UUID questId) {
        adminQuestService.getEntityOrThrow(questId);
        return transitionRepository.findByQuestId(questId).stream()
            .map(this::toTransitionResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransitionResponse> listOutgoing(UUID questId, UUID nodeId) {
        if (!nodeRepository.existsByQuestIdAndId(questId, nodeId)) {
            throw new ResourceNotFoundException("노드", nodeId);
        }
        return transitionRepository.findByFromNodeId(nodeId).stream()
            .map(this::toTransitionResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransitionResponse> listIncoming(UUID questId, UUID nodeId) {
        if (!nodeRepository.existsByQuestIdAndId(questId, nodeId)) {
            throw new ResourceNotFoundException("노드", nodeId);
        }
        return transitionRepository.findByToNodeId(nodeId).stream()
            .map(this::toTransitionResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransitionResponse get(UUID questId, UUID transitionId) {
        adminQuestService.getEntityOrThrow(questId);
        NodeTransition t = transitionRepository.findById(transitionId)
            .orElseThrow(() -> new ResourceNotFoundException("전환", transitionId));
        if (!t.getFromNode().getQuest().getId().equals(questId)) {
            throw new ResourceNotFoundException("전환", transitionId);
        }
        return toTransitionResponse(t);
    }

    @Transactional
    public TransitionResponse create(UUID questId, TransitionCreateRequest req) {
        adminQuestService.getEntityOrThrow(questId);
        QuestNode fromNode = nodeRepository.findById(req.getFromNodeId())
            .orElseThrow(() -> new ResourceNotFoundException("출발 노드", req.getFromNodeId()));
        QuestNode toNode = nodeRepository.findById(req.getToNodeId())
            .orElseThrow(() -> new ResourceNotFoundException("도착 노드", req.getToNodeId()));
        if (!fromNode.getQuest().getId().equals(questId) || !toNode.getQuest().getId().equals(questId)) {
            throw new ValidationException("출발/도착 노드는 모두 해당 퀘스트에 속해야 합니다.");
        }
        NodeTransition t = NodeTransition.create(
            fromNode,
            toNode,
            req.getTransitionOrder(),
            req.getMessageType(),
            req.getTextContent(),
            req.getAudioUrl(),
            req.getLanguage()
        );
        t = transitionRepository.save(t);
        return toTransitionResponse(t);
    }

    @Transactional
    public TransitionResponse update(UUID questId, UUID transitionId, TransitionUpdateRequest req) {
        adminQuestService.getEntityOrThrow(questId);
        NodeTransition t = transitionRepository.findById(transitionId)
            .orElseThrow(() -> new ResourceNotFoundException("전환", transitionId));
        if (!t.getFromNode().getQuest().getId().equals(questId)) {
            throw new ResourceNotFoundException("전환", transitionId);
        }
        if (req.getTransitionOrder() != null) t.setTransitionOrder(req.getTransitionOrder());
        if (req.getMessageType() != null) t.setMessageType(req.getMessageType());
        if (req.getTextContent() != null) t.setTextContent(req.getTextContent());
        if (req.getAudioUrl() != null) t.setAudioUrl(req.getAudioUrl());
        if (req.getLanguage() != null) t.setLanguage(req.getLanguage());
        t = transitionRepository.save(t);
        return toTransitionResponse(t);
    }

    @Transactional
    public void delete(UUID questId, UUID transitionId) {
        adminQuestService.getEntityOrThrow(questId);
        NodeTransition t = transitionRepository.findById(transitionId)
            .orElseThrow(() -> new ResourceNotFoundException("전환", transitionId));
        if (!t.getFromNode().getQuest().getId().equals(questId)) {
            throw new ResourceNotFoundException("전환", transitionId);
        }
        transitionRepository.delete(t);
    }

    private TransitionResponse toTransitionResponse(NodeTransition t) {
        return TransitionResponse.builder()
            .id(t.getId())
            .fromNodeId(t.getFromNode().getId())
            .toNodeId(t.getToNode().getId())
            .transitionOrder(t.getTransitionOrder())
            .messageType(t.getMessageType())
            .textContent(t.getTextContent())
            .audioUrl(t.getAudioUrl())
            .language(t.getLanguage())
            .createdAt(t.getCreatedAt())
            .build();
    }
}

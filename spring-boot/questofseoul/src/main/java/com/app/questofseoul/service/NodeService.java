package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.NodeContent;
import com.app.questofseoul.domain.entity.QuestNode;
import com.app.questofseoul.domain.entity.UserQuestState;
import com.app.questofseoul.domain.enums.DisplayMode;
import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.dto.*;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeService {

    private final QuestNodeRepository nodeRepository;
    private final NodeContentRepository contentRepository;
    private final UserQuestStateRepository stateRepository;
    private final NodeUnlockService unlockService;

    @Transactional(readOnly = true)
    public Optional<CurrentNodeResponse> getCurrentNode(UUID userId, UUID questId) {
        Optional<UserQuestState> stateOpt = stateRepository.findByUserIdAndQuestId(userId, questId);
        if (stateOpt.isEmpty()) {
            return Optional.empty();
        }

        UserQuestState state = stateOpt.get();
        QuestNode node = state.getCurrentNode();
        
        // If current node is null or completed, find next unlocked node
        if (node == null || isNodeCompleted(userId, questId, node.getId())) {
            node = findNextUnlockedNode(userId, questId, state);
            if (node == null) {
                return Optional.empty();
            }
        }

        List<NodeContent> contents = contentRepository.findByNodeIdAndLanguageOrderByContentOrder(
            node.getId(), Language.KO);
        boolean hasContent = !contents.isEmpty();

        Double latitude = null;
        Double longitude = null;
        if (node.getGeo() != null) {
            latitude = node.getGeo().getY();
            longitude = node.getGeo().getX();
        }

        return Optional.of(CurrentNodeResponse.builder()
            .nodeId(node.getId())
            .nodeType(node.getNodeType())
            .title(node.getTitle())
            .latitude(latitude)
            .longitude(longitude)
            .hasContent(hasContent)
            .build());
    }

    private boolean isNodeCompleted(UUID userId, UUID questId, UUID nodeId) {
        // Node is considered completed if:
        // 1. All contents are consumed AND
        // 2. All actions are completed (or no actions exist)
        // For now, simplified logic - will be enhanced
        return false;
    }

    private QuestNode findNextUnlockedNode(UUID userId, UUID questId, UserQuestState state) {
        List<QuestNode> unlockedNodes = unlockService.getUnlockedNodes(userId, questId);
        
        // Find the first unlocked node that hasn't been completed
        // For now, return the first unlocked node after current order
        Integer currentOrder = state.getCurrentNode() != null ? 
            state.getCurrentNode().getOrderIndex() : -1;
        
        return unlockedNodes.stream()
            .filter(n -> n.getOrderIndex() > currentOrder)
            .findFirst()
            .orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<NodeContentsResponse> getNodeContents(UUID nodeId, Language language) {
        QuestNode node = nodeRepository.findById(nodeId)
            .orElseThrow(() -> new ResourceNotFoundException("노드", nodeId));

        List<NodeContent> contents = contentRepository.findByNodeIdAndLanguageOrderByContentOrder(
            nodeId, language);

        if (contents.isEmpty()) {
            return Optional.empty();
        }

        List<NodeContentResponse> contentResponses = contents.stream()
            .map(this::toContentResponse)
            .collect(Collectors.toList());

        return Optional.of(NodeContentsResponse.builder()
            .nodeId(nodeId)
            .title(node.getTitle())
            .contents(contentResponses)
            .totalContents(contents.size())
            .currentContentOrder(1)
            .build());
    }

    @Transactional(readOnly = true)
    public Optional<NodeContentsResponse> getCurrentNodeContents(UUID userId, UUID questId, Language language) {
        Optional<UserQuestState> stateOpt = stateRepository.findByUserIdAndQuestId(userId, questId);
        if (stateOpt.isEmpty()) {
            return Optional.empty();
        }

        UserQuestState state = stateOpt.get();
        QuestNode node = state.getCurrentNode();
        
        // If current node is null, find next unlocked node
        if (node == null) {
            node = findNextUnlockedNode(userId, questId, state);
            if (node == null) {
                return Optional.empty();
            }
        }

        Optional<NodeContentsResponse> response = getNodeContents(node.getId(), language);
        
        // Update currentContentOrder from state
        if (response.isPresent() && state.getCurrentContentOrder() != null) {
            NodeContentsResponse original = response.get();
            return Optional.of(NodeContentsResponse.builder()
                .nodeId(original.getNodeId())
                .title(original.getTitle())
                .contents(original.getContents())
                .totalContents(original.getTotalContents())
                .currentContentOrder(state.getCurrentContentOrder())
                .build());
        }
        
        return response;
    }

    private NodeContentResponse toContentResponse(NodeContent content) {
        boolean isFirst = content.getContentOrder() == 1;

        NodeContentResponse.TextContent textContent = NodeContentResponse.TextContent.builder()
            .script(content.getBody())
            .displayMode(content.getDisplayMode())
            .build();

        NodeContentResponse.AudioContent audioContent = NodeContentResponse.AudioContent.builder()
            .audioUrl(content.getAudioUrl())
            .durationSec(null) // TODO: Calculate from audio file if needed
            .autoPlay(isFirst)
            .build();

        NodeContentResponse.UIHints uiHints = NodeContentResponse.UIHints.builder()
            .showSubtitle(true)
            .allowSpeedControl(true)
            .allowReplay(true)
            .build();

        return NodeContentResponse.builder()
            .contentId(content.getId())
            .contentOrder(content.getContentOrder())
            .text(textContent)
            .audio(audioContent)
            .uiHints(uiHints)
            .build();
    }
}

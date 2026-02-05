package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.EffectType;
import com.app.questofseoul.dto.ActionSubmitRequest;
import com.app.questofseoul.dto.ActionSubmitResponse;
import com.app.questofseoul.dto.NodeActionsResponse;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionService {

    private final NodeActionRepository actionRepository;
    private final UserQuestHistoryRepository historyRepository;
    private final UserQuestStateRepository stateRepository;
    private final QuestNodeRepository nodeRepository;
    private final NodeUnlockService unlockService;

    @Transactional
    public ActionSubmitResponse submitAction(UUID userId, UUID questId, UUID nodeId, 
                                            UUID actionId, ActionSubmitRequest request) {
        log.info("Submitting action for user: {}, quest: {}, node: {}, action: {}", 
                userId, questId, nodeId, actionId);

        UserQuestState state = stateRepository.findByUserIdAndQuestId(userId, questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트 상태", questId));

        QuestNode node = nodeRepository.findById(nodeId)
            .orElseThrow(() -> new ResourceNotFoundException("노드", nodeId));

        NodeAction action = actionRepository.findById(actionId)
            .orElseThrow(() -> new ResourceNotFoundException("액션", actionId));

        // Save history
        Quest quest = state.getQuest();
        UserQuestHistory history = UserQuestHistory.create(
            userId, quest, node, action, action.getActionType(),
            request.getUserInput(), request.getPhotoUrl(), request.getSelectedOption()
        );
        historyRepository.save(history);

        // Process effects
        List<ActionEffect> effects = action.getEffects();
        Map<String, Object> currentState = state.getState() != null ? 
            new HashMap<>(state.getState()) : new HashMap<>();
        
        List<ActionSubmitResponse.EffectResponse> effectResponses = new ArrayList<>();
        UUID nextNodeUnlocked = null;

        for (ActionEffect effect : effects) {
            Map<String, Object> effectValue = effect.getEffectValue();
            
            if (effect.getEffectType() == EffectType.TAG) {
                String tag = (String) effectValue.get("tag");
                List<String> tags = (List<String>) currentState.getOrDefault("tags", new ArrayList<>());
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
                currentState.put("tags", tags);
                effectResponses.add(ActionSubmitResponse.EffectResponse.builder()
                    .type("tag")
                    .value(tag)
                    .build());
            } else if (effect.getEffectType() == EffectType.MEMORY) {
                List<String> memories = (List<String>) currentState.getOrDefault("memories", new ArrayList<>());
                String memory = (String) effectValue.get("note");
                if (memory != null) {
                    memories.add(memory);
                }
                currentState.put("memories", memories);
                effectResponses.add(ActionSubmitResponse.EffectResponse.builder()
                    .type("memory")
                    .value(effectValue)
                    .build());
            } else if (effect.getEffectType() == EffectType.PROGRESS) {
                String unlockNodeId = (String) effectValue.get("unlock_node");
                if (unlockNodeId != null) {
                    nextNodeUnlocked = UUID.fromString(unlockNodeId);
                }
                effectResponses.add(ActionSubmitResponse.EffectResponse.builder()
                    .type("progress")
                    .value(effectValue)
                    .build());
            }
        }

        // Update state
        state.updateState(currentState);
        
        // Find next unlocked node
        QuestNode nextNode = null;
        if (nextNodeUnlocked != null) {
            final UUID targetNodeId = nextNodeUnlocked;
            nextNode = nodeRepository.findById(targetNodeId)
                .orElseThrow(() -> new ResourceNotFoundException("다음 노드", targetNodeId));
        } else {
            // Auto-find next unlocked node based on unlock conditions
            nextNode = findNextUnlockedNode(userId, questId, state);
        }
        
        if (nextNode != null) {
            state.updateCurrentNode(nextNode);
            nextNodeUnlocked = nextNode.getId();
        }
        
        stateRepository.save(state);

        return ActionSubmitResponse.builder()
            .success(true)
            .effects(effectResponses)
            .nextNodeUnlocked(nextNodeUnlocked)
            .build();
    }

    @Transactional(readOnly = true)
    public Optional<NodeActionsResponse> getNodeActions(UUID nodeId) {
        com.app.questofseoul.domain.entity.QuestNode node = nodeRepository.findById(nodeId)
            .orElseThrow(() -> new ResourceNotFoundException("노드", nodeId));

        List<com.app.questofseoul.domain.entity.NodeAction> actions = actionRepository.findByNodeId(nodeId);

        List<NodeActionsResponse.ActionInfo> actionInfos = actions.stream()
            .map(action -> NodeActionsResponse.ActionInfo.builder()
                .actionId(action.getId())
                .actionType(action.getActionType())
                .prompt(action.getPrompt())
                .options(action.getOptions())
                .build())
            .collect(Collectors.toList());

        return Optional.of(NodeActionsResponse.builder()
            .nodeId(nodeId)
            .nodeTitle(node.getTitle())
            .actions(actionInfos)
            .build());
    }

    private QuestNode findNextUnlockedNode(UUID userId, UUID questId, UserQuestState state) {
        List<QuestNode> unlockedNodes = unlockService.getUnlockedNodes(userId, questId);
        Integer currentOrder = state.getCurrentNode() != null ? 
            state.getCurrentNode().getOrderIndex() : -1;
        
        return unlockedNodes.stream()
            .filter(n -> n.getOrderIndex() > currentOrder)
            .findFirst()
            .orElse(null);
    }
}

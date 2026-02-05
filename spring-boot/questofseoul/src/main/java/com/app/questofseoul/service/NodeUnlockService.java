package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.QuestNode;
import com.app.questofseoul.domain.entity.UserQuestState;
import com.app.questofseoul.repository.QuestNodeRepository;
import com.app.questofseoul.repository.UserQuestHistoryRepository;
import com.app.questofseoul.repository.UserQuestStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeUnlockService {

    private final QuestNodeRepository nodeRepository;
    private final UserQuestHistoryRepository historyRepository;
    private final UserQuestStateRepository stateRepository;

    @Transactional(readOnly = true)
    public List<QuestNode> getUnlockedNodes(UUID userId, UUID questId) {
        Optional<UserQuestState> stateOpt = stateRepository.findByUserIdAndQuestId(userId, questId);
        if (stateOpt.isEmpty()) {
            return Collections.emptyList();
        }

        UserQuestState state = stateOpt.get();
        List<QuestNode> allNodes = nodeRepository.findByQuestIdOrderByOrderIndex(questId);
        Map<String, Object> userState = state.getState() != null ? state.getState() : new HashMap<>();

        return allNodes.stream()
            .filter(node -> isNodeUnlocked(node, state, userState))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isNodeUnlocked(QuestNode node, UserQuestState state, Map<String, Object> userState) {
        if (node.getUnlockCondition() == null) {
            return true;
        }

        Map<String, Object> requires = (Map<String, Object>) node.getUnlockCondition().get("requires");
        if (requires == null) {
            return true;
        }

        // Check completed_nodes
        List<String> requiredNodes = (List<String>) requires.get("completed_nodes");
        if (requiredNodes != null && !requiredNodes.isEmpty()) {
            Set<UUID> completedNodeIds = historyRepository
                .findByUserIdAndQuestIdOrderByCreatedAt(state.getUserId(), state.getQuest().getId())
                .stream()
                .map(h -> h.getNode().getId())
                .collect(Collectors.toSet());

            for (String requiredNodeId : requiredNodes) {
                UUID requiredUuid = UUID.fromString(requiredNodeId);
                if (!completedNodeIds.contains(requiredUuid)) {
                    return false;
                }
            }
        }

        // Check tags
        List<String> requiredTags = (List<String>) requires.get("tags");
        if (requiredTags != null && !requiredTags.isEmpty()) {
            List<String> userTags = (List<String>) userState.getOrDefault("tags", new ArrayList<>());
            for (String requiredTag : requiredTags) {
                if (!userTags.contains(requiredTag)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isNodeUnlocked(QuestNode node, UserQuestState state) {
        Map<String, Object> userState = state.getState() != null ? state.getState() : new HashMap<>();
        return isNodeUnlocked(node, state, userState);
    }
}

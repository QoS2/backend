package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.dto.QuestHistoryResponse;
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
public class HistoryService {

    private final UserQuestHistoryRepository historyRepository;
    private final QuestRepository questRepository;

    @Transactional(readOnly = true)
    public Optional<QuestHistoryResponse> getQuestHistory(UUID userId, UUID questId) {
        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트", questId));

        List<UserQuestHistory> histories = historyRepository.findByUserIdAndQuestIdOrderByCreatedAt(
            userId, questId);

        // Group by node
        Map<UUID, List<UserQuestHistory>> historyByNode = histories.stream()
            .collect(Collectors.groupingBy(h -> h.getNode().getId()));

        List<QuestHistoryResponse.NodeHistory> nodeHistories = historyByNode.entrySet().stream()
            .map(entry -> {
                UUID nodeId = entry.getKey();
                List<UserQuestHistory> nodeHistoryList = entry.getValue();
                
                QuestNode node = nodeHistoryList.get(0).getNode();
                
                List<QuestHistoryResponse.ActionHistory> actionHistories = nodeHistoryList.stream()
                    .map(h -> QuestHistoryResponse.ActionHistory.builder()
                        .actionType(h.getActionType())
                        .userInput(h.getUserInput())
                        .photoUrl(h.getPhotoUrl())
                        .selectedOption(h.getSelectedOption())
                        .createdAt(h.getCreatedAt())
                        .build())
                    .collect(Collectors.toList());

                return QuestHistoryResponse.NodeHistory.builder()
                    .nodeId(nodeId)
                    .nodeTitle(node.getTitle())
                    .actions(actionHistories)
                    .build();
            })
            .sorted(Comparator.comparing(h -> {
                // Sort by first action timestamp
                return h.getActions().get(0).getCreatedAt();
            }))
            .collect(Collectors.toList());

        return Optional.of(QuestHistoryResponse.builder()
            .questId(questId)
            .questTitle(quest.getTitle())
            .history(nodeHistories)
            .build());
    }
}

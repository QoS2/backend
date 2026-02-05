package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.NodeContent;
import com.app.questofseoul.domain.entity.QuestNode;
import com.app.questofseoul.domain.entity.UserQuestState;
import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.dto.ContentCompleteResponse;
import com.app.questofseoul.exception.BusinessException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final NodeContentRepository contentRepository;
    private final NodeActionRepository actionRepository;
    private final UserQuestStateRepository stateRepository;

    @Transactional
    public ContentCompleteResponse completeContent(UUID userId, UUID questId, UUID nodeId, 
                                                  UUID contentId, Integer contentOrder) {
        log.info("Completing content for user: {}, quest: {}, node: {}, content: {}", 
                userId, questId, nodeId, contentId);

        UserQuestState state = stateRepository.findByUserIdAndQuestId(userId, questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트 상태", questId));

        QuestNode node = state.getCurrentNode();
        if (node == null || !node.getId().equals(nodeId)) {
            throw new BusinessException("현재 노드와 요청한 노드가 일치하지 않습니다");
        }

        // Check if there are more contents
        List<NodeContent> contents = contentRepository.findByNodeIdAndLanguageOrderByContentOrder(
            nodeId, Language.KO);
        
        boolean hasNextContent = contentOrder < contents.size();
        Integer nextContentOrder = hasNextContent ? contentOrder + 1 : null;

        // Update content order
        if (hasNextContent) {
            state.updateContentOrder(nextContentOrder);
        } else {
            // Check if there are actions available
            boolean hasActions = !actionRepository.findByNodeId(nodeId).isEmpty();
            state.updateContentOrder(null);
        }

        stateRepository.save(state);

        // Check if next action is enabled (all contents consumed and actions exist)
        boolean nextActionEnabled = !hasNextContent && 
            !actionRepository.findByNodeId(nodeId).isEmpty();

        return ContentCompleteResponse.builder()
            .hasNextContent(hasNextContent)
            .nextContentOrder(nextContentOrder)
            .nextActionEnabled(nextActionEnabled)
            .build();
    }
}

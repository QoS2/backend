package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.QuestStatus;
import com.app.questofseoul.exception.BusinessException;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestStateService {

    private final UserQuestStateRepository stateRepository;
    private final QuestRepository questRepository;
    private final QuestNodeRepository nodeRepository;

    @Transactional
    public UserQuestState startQuest(UUID userId, UUID questId) {
        log.info("Starting quest for user: {}, quest: {}", userId, questId);
        
        Optional<UserQuestState> existing = stateRepository.findByUserIdAndQuestId(userId, questId);
        if (existing.isPresent()) {
            log.info("Quest already started, returning existing state");
            return existing.get();
        }

        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트", questId));

        List<QuestNode> nodes = nodeRepository.findByQuestIdOrderByOrderIndex(questId);
        if (nodes.isEmpty()) {
            throw new BusinessException("퀘스트에 노드가 없습니다: " + questId);
        }

        QuestNode firstNode = nodes.get(0);
        UserQuestState state = UserQuestState.create(userId, quest, firstNode);
        
        return stateRepository.save(state);
    }

    @Transactional(readOnly = true)
    public Optional<UserQuestState> getCurrentState(UUID userId, UUID questId) {
        return stateRepository.findByUserIdAndQuestId(userId, questId);
    }

    @Transactional
    public void updateContentOrder(UUID userId, UUID questId, Integer contentOrder) {
        UserQuestState state = stateRepository.findByUserIdAndQuestId(userId, questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트 상태", questId));
        
        state.updateContentOrder(contentOrder);
        stateRepository.save(state);
    }

    @Transactional
    public void updateState(UUID userId, UUID questId, Map<String, Object> newState) {
        UserQuestState state = stateRepository.findByUserIdAndQuestId(userId, questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트 상태", questId));
        
        state.updateState(newState);
        stateRepository.save(state);
    }

    @Transactional
    public com.app.questofseoul.dto.QuestCompletionResponse completeQuest(UUID userId, UUID questId) {
        UserQuestState state = stateRepository.findByUserIdAndQuestId(userId, questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트 상태", questId));
        
        state.complete();
        stateRepository.save(state);
        log.info("Quest completed for user: {}, quest: {}", userId, questId);

        Quest quest = state.getQuest();
        return com.app.questofseoul.dto.QuestCompletionResponse.builder()
            .questId(questId)
            .questTitle(quest.getTitle())
            .completedAt(state.getCompletedAt())
            .reportReady(true) // AI 리포트는 FastAPI에서 생성
            .reportUrl(null) // FastAPI 리포트 URL은 별도로 설정
            .build();
    }
}

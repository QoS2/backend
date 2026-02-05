package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.NodeTransition;
import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.dto.TransitionMessageResponse;
import com.app.questofseoul.repository.NodeTransitionRepository;
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
public class TransitionService {

    private final NodeTransitionRepository transitionRepository;

    @Transactional(readOnly = true)
    public Optional<TransitionMessageResponse> getTransitionMessages(UUID fromNodeId, UUID toNodeId, 
                                                                    Language language) {
        List<NodeTransition> transitions = transitionRepository
            .findByFromNodeIdAndToNodeIdAndLanguageOrderByTransitionOrder(
                fromNodeId, toNodeId, language != null ? language : Language.KO);

        if (transitions.isEmpty()) {
            return Optional.empty();
        }

        List<TransitionMessageResponse.Message> messages = transitions.stream()
            .map(t -> TransitionMessageResponse.Message.builder()
                .transitionOrder(t.getTransitionOrder())
                .messageType(t.getMessageType())
                .textContent(t.getTextContent())
                .audioUrl(t.getAudioUrl())
                .build())
            .collect(Collectors.toList());

        return Optional.of(TransitionMessageResponse.builder()
            .fromNodeId(fromNodeId)
            .toNodeId(toNodeId)
            .messages(messages)
            .build());
    }
}

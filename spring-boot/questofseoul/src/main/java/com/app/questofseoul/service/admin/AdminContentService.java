package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.NodeContent;
import com.app.questofseoul.domain.entity.QuestNode;
import com.app.questofseoul.dto.admin.ContentCreateRequest;
import com.app.questofseoul.dto.admin.ContentResponse;
import com.app.questofseoul.dto.admin.ContentUpdateRequest;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.NodeContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final NodeContentRepository contentRepository;
    private final AdminNodeService adminNodeService;

    @Transactional(readOnly = true)
    public List<ContentResponse> listByNode(UUID questId, UUID nodeId) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        return contentRepository.findByNodeIdOrderByContentOrder(nodeId).stream()
            .map(this::toContentResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContentResponse get(UUID questId, UUID nodeId, UUID contentId) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        NodeContent content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("콘텐츠", contentId));
        if (!content.getNode().getId().equals(nodeId)) {
            throw new ResourceNotFoundException("콘텐츠", contentId);
        }
        return toContentResponse(content);
    }

    @Transactional
    public ContentResponse create(UUID questId, UUID nodeId, ContentCreateRequest req) {
        QuestNode node = adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        NodeContent content = NodeContent.create(
            node,
            req.getContentOrder(),
            req.getContentType(),
            req.getLanguage(),
            req.getBody(),
            req.getAudioUrl(),
            req.getVoiceStyle(),
            req.getDisplayMode()
        );
        content = contentRepository.save(content);
        return toContentResponse(content);
    }

    @Transactional
    public ContentResponse update(UUID questId, UUID nodeId, UUID contentId, ContentUpdateRequest req) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        NodeContent content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("콘텐츠", contentId));
        if (!content.getNode().getId().equals(nodeId)) {
            throw new ResourceNotFoundException("콘텐츠", contentId);
        }
        if (req.getContentOrder() != null) content.setContentOrder(req.getContentOrder());
        if (req.getContentType() != null) content.setContentType(req.getContentType());
        if (req.getLanguage() != null) content.setLanguage(req.getLanguage());
        if (req.getBody() != null) content.setBody(req.getBody());
        if (req.getAudioUrl() != null) content.setAudioUrl(req.getAudioUrl());
        if (req.getVoiceStyle() != null) content.setVoiceStyle(req.getVoiceStyle());
        if (req.getDisplayMode() != null) content.setDisplayMode(req.getDisplayMode());
        content = contentRepository.save(content);
        return toContentResponse(content);
    }

    @Transactional
    public void delete(UUID questId, UUID nodeId, UUID contentId) {
        adminNodeService.getNodeInQuestOrThrow(questId, nodeId);
        NodeContent content = contentRepository.findById(contentId)
            .orElseThrow(() -> new ResourceNotFoundException("콘텐츠", contentId));
        if (!content.getNode().getId().equals(nodeId)) {
            throw new ResourceNotFoundException("콘텐츠", contentId);
        }
        contentRepository.delete(content);
    }

    private ContentResponse toContentResponse(NodeContent c) {
        return ContentResponse.builder()
            .id(c.getId())
            .nodeId(c.getNode().getId())
            .contentOrder(c.getContentOrder())
            .contentType(c.getContentType())
            .language(c.getLanguage())
            .body(c.getBody())
            .audioUrl(c.getAudioUrl())
            .voiceStyle(c.getVoiceStyle())
            .displayMode(c.getDisplayMode())
            .createdAt(c.getCreatedAt())
            .build();
    }
}

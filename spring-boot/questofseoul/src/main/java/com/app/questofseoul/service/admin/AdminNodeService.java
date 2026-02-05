package com.app.questofseoul.service.admin;

import com.app.questofseoul.domain.entity.Quest;
import com.app.questofseoul.domain.entity.QuestNode;
import com.app.questofseoul.domain.enums.NodeType;
import com.app.questofseoul.dto.admin.NodeCreateRequest;
import com.app.questofseoul.dto.admin.NodeResponse;
import com.app.questofseoul.dto.admin.NodeReorderRequest;
import com.app.questofseoul.dto.admin.NodeUpdateRequest;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.exception.ValidationException;
import com.app.questofseoul.repository.QuestNodeRepository;
import com.app.questofseoul.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNodeService {

    private final QuestRepository questRepository;
    private final QuestNodeRepository nodeRepository;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional(readOnly = true)
    public List<NodeResponse> listByQuest(UUID questId) {
        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트", questId));
        return nodeRepository.findByQuestIdOrderByOrderIndex(quest.getId()).stream()
            .map(this::toNodeResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NodeResponse get(UUID questId, UUID nodeId) {
        QuestNode node = getNodeInQuestOrThrow(questId, nodeId);
        return toNodeResponse(node);
    }

    @Transactional
    public NodeResponse create(UUID questId, NodeCreateRequest req) {
        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new ResourceNotFoundException("퀘스트", questId));
        var point = pointOrNull(req.getGeoLatitude(), req.getGeoLongitude());
        QuestNode node = QuestNode.create(
            quest,
            req.getNodeType(),
            req.getTitle(),
            req.getOrderIndex(),
            point,
            req.getUnlockCondition()
        );
        node = nodeRepository.save(node);
        return toNodeResponse(node);
    }

    @Transactional
    public NodeResponse update(UUID questId, UUID nodeId, NodeUpdateRequest req) {
        QuestNode node = getNodeInQuestOrThrow(questId, nodeId);
        if (req.getNodeType() != null) node.setNodeType(req.getNodeType());
        if (req.getTitle() != null) node.setTitle(req.getTitle());
        if (req.getOrderIndex() != null) node.setOrderIndex(req.getOrderIndex());
        if (req.getGeoLatitude() != null && req.getGeoLongitude() != null) {
            node.setGeo(pointOrNull(req.getGeoLatitude(), req.getGeoLongitude()));
        }
        if (req.getUnlockCondition() != null) node.setUnlockCondition(req.getUnlockCondition());
        node = nodeRepository.save(node);
        return toNodeResponse(node);
    }

    @Transactional
    public void delete(UUID questId, UUID nodeId) {
        QuestNode node = getNodeInQuestOrThrow(questId, nodeId);
        nodeRepository.delete(node);
    }

    @Transactional
    public void reorder(UUID questId, NodeReorderRequest req) {
        for (NodeReorderRequest.NodeOrderItem item : req.getNodes()) {
            if (!nodeRepository.existsByQuestIdAndId(questId, item.getNodeId())) {
                throw new ValidationException("노드가 해당 퀘스트에 속하지 않습니다: " + item.getNodeId());
            }
            QuestNode node = nodeRepository.findById(item.getNodeId())
                .orElseThrow(() -> new ResourceNotFoundException("노드", item.getNodeId()));
            node.setOrderIndex(item.getOrderIndex());
            nodeRepository.save(node);
        }
    }

    QuestNode getNodeInQuestOrThrow(UUID questId, UUID nodeId) {
        if (!nodeRepository.existsByQuestIdAndId(questId, nodeId)) {
            throw new ResourceNotFoundException("노드", nodeId);
        }
        return nodeRepository.findById(nodeId)
            .orElseThrow(() -> new ResourceNotFoundException("노드", nodeId));
    }

    private NodeResponse toNodeResponse(QuestNode node) {
        var builder = NodeResponse.builder()
            .id(node.getId())
            .questId(node.getQuest().getId())
            .nodeType(node.getNodeType())
            .title(node.getTitle())
            .orderIndex(node.getOrderIndex())
            .unlockCondition(node.getUnlockCondition())
            .createdAt(node.getCreatedAt());
        if (node.getGeo() != null) {
            builder.geoLatitude(node.getGeo().getY()).geoLongitude(node.getGeo().getX());
        }
        return builder.build();
    }

    private static org.locationtech.jts.geom.Point pointOrNull(Double lat, Double lon) {
        if (lat == null || lon == null) return null;
        return GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
    }
}

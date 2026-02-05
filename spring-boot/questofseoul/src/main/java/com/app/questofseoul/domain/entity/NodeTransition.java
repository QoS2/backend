package com.app.questofseoul.domain.entity;

import com.app.questofseoul.domain.enums.Language;
import com.app.questofseoul.domain.enums.TransitionMessageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "node_transitions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NodeTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_node_id", nullable = false)
    private QuestNode fromNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_node_id", nullable = false)
    private QuestNode toNode;

    @Column(name = "transition_order", nullable = false)
    private Integer transitionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private TransitionMessageType messageType;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "audio_url")
    private String audioUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language = Language.KO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static NodeTransition create(QuestNode fromNode, QuestNode toNode, 
                                       Integer transitionOrder, 
                                       TransitionMessageType messageType, 
                                       String textContent, String audioUrl, 
                                       Language language) {
        NodeTransition transition = new NodeTransition();
        transition.fromNode = fromNode;
        transition.toNode = toNode;
        transition.transitionOrder = transitionOrder;
        transition.messageType = messageType;
        transition.textContent = textContent;
        transition.audioUrl = audioUrl;
        transition.language = language != null ? language : Language.KO;
        return transition;
    }

    public void setTransitionOrder(Integer transitionOrder) { this.transitionOrder = transitionOrder; }
    public void setMessageType(TransitionMessageType messageType) { this.messageType = messageType; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public void setLanguage(Language language) { this.language = language != null ? language : Language.KO; }
}

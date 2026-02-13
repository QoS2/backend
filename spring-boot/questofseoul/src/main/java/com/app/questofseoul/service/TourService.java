package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.*;
import com.app.questofseoul.domain.enums.*;
import com.app.questofseoul.dto.tour.*;
import com.app.questofseoul.exception.ResourceNotFoundException;
import com.app.questofseoul.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourService {

    private static final double EARTH_RADIUS_M = 6371000;
    private static final int PROXIMITY_RADIUS_M = 50;

    private final TourGuideAiService tourGuideAiService;
    private final TourRepository tourRepository;
    private final StepRepository stepRepository;
    private final WaypointRepository waypointRepository;
    private final PhotoSpotRepository photoSpotRepository;
    private final TreasureRepository treasureRepository;
    private final ChatContentRepository chatContentRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatTurnRepository chatTurnRepository;
    private final TourRunRepository tourRunRepository;
    private final GuideContentRepository guideContentRepository;
    private final QuizRepository quizRepository;
    private final MediaAssetRepository mediaAssetRepository;

    @Transactional(readOnly = true)
    public List<com.app.questofseoul.dto.tour.TourListItem> listTours() {
        return tourRepository.findAll().stream()
            .map(t -> new com.app.questofseoul.dto.tour.TourListItem(t.getId(), t.getExternalKey(), t.getTitleEn()))
            .toList();
    }

    @Transactional(readOnly = true)
    public TourDetailResponse getTourDetail(Long tourId, java.util.UUID userId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        List<Step> steps = stepRepository.findByTourIdOrderByStepOrderAsc(tourId);
        List<Waypoint> waypoints = waypointRepository.findByTourId(tourId);
        List<PhotoSpot> photoSpots = photoSpotRepository.findByTourId(tourId);
        List<Treasure> treasures = treasureRepository.findByTourId(tourId);

        int quizzesCount = (int) steps.stream()
            .mapToLong(s -> quizRepository.findByStepIdOrderByIdAsc(s.getId()).size())
            .sum();

        BigDecimal startLat = null;
        BigDecimal startLng = null;
        if (!steps.isEmpty() && steps.get(0).getLatitude() != null) {
            startLat = steps.get(0).getLatitude();
            startLng = steps.get(0).getLongitude();
        }

        boolean unlocked = userId != null; // TODO: 결제/구독 로직

        return new TourDetailResponse(
            tour.getId(),
            tour.getExternalKey(),
            tour.getTitleEn(),
            tour.getDescriptionEn(),
            tour.getInfoJson(),
            tour.getGoodToKnowJson(),
            Collections.emptyList(),
            steps.size(),
            waypoints.size(),
            photoSpots.size(),
            treasures.size(),
            quizzesCount,
            startLat,
            startLng,
            unlocked
        );
    }

    @Transactional(readOnly = true)
    public List<MarkerResponse> getMarkers(Long tourId, MarkerType filter) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        List<MarkerResponse> result = new ArrayList<>();

        if (filter == null || filter == MarkerType.STEP) {
            List<Step> steps = stepRepository.findByTourIdOrderByStepOrderAsc(tourId);
            for (Step s : steps) {
                if (s.getLatitude() != null && s.getLongitude() != null)
                    result.add(new MarkerResponse(s.getId(), MarkerType.STEP, s.getTitleEn(),
                        s.getLatitude(), s.getLongitude(), s.getRadiusM(), s.getId(), s.getStepOrder()));
            }
        }
        if (filter == null || filter == MarkerType.WAYPOINT) {
            List<Waypoint> waypoints = waypointRepository.findByTourId(tourId);
            for (Waypoint w : waypoints) {
                if (w.getLatitude() != null && w.getLongitude() != null)
                    result.add(new MarkerResponse(w.getId(), MarkerType.WAYPOINT, w.getTitleEn(),
                        w.getLatitude(), w.getLongitude(), w.getRadiusM(), w.getStep() != null ? w.getStep().getId() : null, null));
            }
        }
        if (filter == null || filter == MarkerType.PHOTO_SPOT) {
            List<PhotoSpot> spots = photoSpotRepository.findByTourId(tourId);
            for (PhotoSpot p : spots) {
                if (p.getLatitude() != null && p.getLongitude() != null)
                    result.add(new MarkerResponse(p.getId(), MarkerType.PHOTO_SPOT, p.getTitleEn(),
                        p.getLatitude(), p.getLongitude(), p.getRadiusM(), p.getStep() != null ? p.getStep().getId() : null, null));
            }
        }
        if (filter == null || filter == MarkerType.TREASURE) {
            List<Treasure> treasures = treasureRepository.findByTourId(tourId);
            for (Treasure t : treasures) {
                if (t.getLatitude() != null && t.getLongitude() != null)
                    result.add(new MarkerResponse(t.getId(), MarkerType.TREASURE, t.getTitleEn(),
                        t.getLatitude(), t.getLongitude(), t.getRadiusM(), t.getStep() != null ? t.getStep().getId() : null, null));
            }
        }

        return result;
    }

    @Transactional
    public com.app.questofseoul.dto.tour.StartTourResponse startTour(User user, Long tourId) {
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        TourRun run = tourRunRepository.save(TourRun.create(user, tour));
        ChatSession generalSession = chatSessionRepository
            .findByTourRunIdAndSessionKind(run.getId(), SessionKind.GENERAL)
            .orElseGet(() -> chatSessionRepository.save(ChatSession.create(user, run, SessionKind.GENERAL, null, null)));
        return new com.app.questofseoul.dto.tour.StartTourResponse(run.getId(), tourId, generalSession.getId());
    }

    public ChatSession getOrCreateGeneralSession(Long tourRunId, User user) {
        TourRun run = tourRunRepository.findById(tourRunId).orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(user.getId())) throw new com.app.questofseoul.exception.AuthorizationException("Not your tour run");
        return chatSessionRepository
            .findByTourRunIdAndSessionKind(tourRunId, SessionKind.GENERAL)
            .orElseGet(() -> chatSessionRepository.save(ChatSession.create(user, run, SessionKind.GENERAL, null, null)));
    }

    @Transactional
    public ProximityResponse checkProximity(Long tourRunId, User user, BigDecimal lat, BigDecimal lng, Language lang) {
        TourRun run = tourRunRepository.findById(tourRunId).orElseThrow(() -> new ResourceNotFoundException("Tour run not found"));
        if (!run.getUser().getId().equals(user.getId())) throw new com.app.questofseoul.exception.AuthorizationException("Not your tour run");

        Long tourId = run.getTour().getId();
        Language language = lang != null ? lang : Language.KO;

        List<MarkerResponse> allMarkers = getMarkers(tourId, null);
        for (MarkerResponse m : allMarkers) {
            double dist = haversineMeters(lat.doubleValue(), lng.doubleValue(),
                m.latitude().doubleValue(), m.longitude().doubleValue());
            int radius = m.radiusM() != null ? m.radiusM() : PROXIMITY_RADIUS_M;
            if (dist <= radius) {
                ChatRefType refType = toChatRefType(m.type());
                ChatContent content = chatContentRepository.findByRefTypeAndRefIdAndLanguage(refType, m.id(), language)
                    .orElse(null);
                if (content == null) continue;

                ChatSession session = chatSessionRepository
                    .findByTourRunIdAndContextRefTypeAndContextRefId(tourRunId, refType, m.id())
                    .orElseGet(() -> chatSessionRepository.save(ChatSession.create(user, run, SessionKind.PROXIMITY, refType, m.id())));

                List<ChatTurn> existingTurns = chatTurnRepository.findBySessionIdOrderByTurnIdxAsc(session.getId());
                long existingScriptCount = existingTurns.stream().filter(t -> t.getSource() == ChatSource.SCRIPT).count();

                if (existingScriptCount > 0) {
                    // 이미 주입됨 - 기존 턴으로 응답
                    List<ProximityResponse.ChatTurnDto> existingDtos = existingTurns.stream()
                        .filter(t -> t.getSource() == ChatSource.SCRIPT)
                        .map(t -> {
                            List<ProximityResponse.AssetDto> assets = List.of();
                            if (t.getMetaJson() != null && t.getMetaJson().containsKey("assets")) {
                                @SuppressWarnings("unchecked")
                                var list = (List<Map<String, Object>>) t.getMetaJson().get("assets");
                                assets = list != null ? list.stream()
                                    .map(a -> new ProximityResponse.AssetDto(
                                        a.get("id") != null ? ((Number) a.get("id")).longValue() : null,
                                        (String) a.getOrDefault("type", "IMAGE"),
                                        (String) a.getOrDefault("url", ""),
                                        a.get("meta")))
                                    .toList() : List.of();
                            }
                            ProximityResponse.ActionDto action = null;
                            if (t.getActionJson() != null && t.getActionJson().containsKey("type")) {
                                String type = (String) t.getActionJson().get("type");
                                String label = (String) t.getActionJson().getOrDefault("label", "View Details");
                                Long stepId = t.getActionJson().get("step_id") != null ?
                                    ((Number) t.getActionJson().get("step_id")).longValue() : null;
                                action = new ProximityResponse.ActionDto(type, label, stepId);
                            }
                            return new ProximityResponse.ChatTurnDto(t.getId(), t.getRole().name(), t.getSource().name(), t.getText(), assets, action);
                        })
                        .toList();
                    return new ProximityResponse("PROXIMITY_TRIGGER", "SCRIPTED_GUIDE", session.getId(),
                        new ProximityResponse.ProximityContext(refType.name(), m.id(), m.title()), existingDtos);
                }

                List<ChatMessage> messages = content.getMessages().stream()
                    .sorted(Comparator.comparing(ChatMessage::getMsgIdx))
                    .toList();

                if (messages.isEmpty()) continue;

                List<ProximityResponse.ChatTurnDto> turnDtos = new ArrayList<>();
                int baseIdx = (int) chatTurnRepository.countBySessionId(session.getId());
                int idx = 0;
                for (ChatMessage msg : messages) {
                    ChatTurn turn = ChatTurn.create(session, baseIdx + idx, ChatRole.GUIDE, ChatSource.SCRIPT,
                        msg.getTextEn(), msg.getActionJson(), toMetaWithAssets(msg));
                    turn = chatTurnRepository.save(turn);
                    idx++;

                    List<ProximityResponse.AssetDto> assets = msg.getAssets().stream()
                        .sorted(Comparator.comparing(a -> a.getSortOrder() != null ? a.getSortOrder() : 0))
                        .map(a -> new ProximityResponse.AssetDto(a.getMediaAsset().getId(), "IMAGE", a.getMediaAsset().getUrlOrKey(), a.getMediaAsset().getMetaJson()))
                        .toList();

                    ProximityResponse.ActionDto action = null;
                    if (msg.getActionJson() != null && msg.getActionJson().containsKey("type")) {
                        String type = (String) msg.getActionJson().get("type");
                        String label = (String) msg.getActionJson().getOrDefault("label", "View Details");
                        Long stepId = msg.getActionJson().get("step_id") != null ?
                            ((Number) msg.getActionJson().get("step_id")).longValue() : null;
                        action = new ProximityResponse.ActionDto(type, label, stepId);
                    }

                    turnDtos.add(new ProximityResponse.ChatTurnDto(turn.getId(), "GUIDE", "SCRIPT", msg.getTextEn(), assets, action));
                }

                return new ProximityResponse(
                    "PROXIMITY_TRIGGER",
                    "SCRIPTED_GUIDE",
                    session.getId(),
                    new ProximityResponse.ProximityContext(refType.name(), m.id(), m.title()),
                    turnDtos
                );
            }
        }
        return null;
    }

    private Map<String, Object> toMetaWithAssets(ChatMessage msg) {
        if (msg.getAssets().isEmpty()) return null;
        List<Map<String, Object>> assets = msg.getAssets().stream()
            .sorted(Comparator.comparing(a -> a.getSortOrder() != null ? a.getSortOrder() : 0))
            .map(a -> Map.<String, Object>of(
                "id", a.getMediaAsset().getId(),
                "type", "IMAGE",
                "url", a.getMediaAsset().getUrlOrKey(),
                "meta", a.getMediaAsset().getMetaJson() != null ? a.getMediaAsset().getMetaJson() : Map.of()
            ))
            .collect(Collectors.toList());
        return Map.of("assets", assets);
    }

    private ChatRefType toChatRefType(MarkerType t) {
        return switch (t) {
            case STEP -> ChatRefType.STEP;
            case WAYPOINT -> ChatRefType.WAYPOINT;
            case PHOTO_SPOT -> ChatRefType.PHOTO_SPOT;
            case TREASURE -> ChatRefType.TREASURE;
        };
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    @Transactional(readOnly = true)
    public ChatTurnsResponse getChatTurns(Long sessionId, User user) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        if (!session.getUser().getId().equals(user.getId())) throw new com.app.questofseoul.exception.AuthorizationException("Not your session");

        List<ChatTurn> turns = chatTurnRepository.findBySessionIdOrderByTurnIdxAsc(sessionId);
        List<ChatTurnsResponse.ChatTurnItem> items = turns.stream()
            .map(t -> new ChatTurnsResponse.ChatTurnItem(
                t.getId(), t.getRole().name(), t.getSource().name(), t.getText(),
                t.getMetaJson(), t.getActionJson(),
                t.getCreatedAt() != null ? t.getCreatedAt().toString() : null
            ))
            .toList();
        return new ChatTurnsResponse(session.getId(), items);
    }

    @Transactional
    public com.app.questofseoul.dto.tour.SendMessageResponse sendUserMessage(Long sessionId, User user, String text) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
        if (!session.getUser().getId().equals(user.getId())) throw new com.app.questofseoul.exception.AuthorizationException("Not your session");

        int nextIdx = (int) chatTurnRepository.countBySessionId(session.getId());
        ChatTurn userTurn = ChatTurn.create(session, nextIdx, ChatRole.USER, ChatSource.USER, text, null, null);
        userTurn = chatTurnRepository.save(userTurn);

        String tourContext = buildTourContextForAi(session.getTourRun().getTour().getId(), session.getContextRefId());
        List<Map<String, String>> history = chatTurnRepository.findBySessionIdOrderByTurnIdxAsc(sessionId).stream()
            .map(t -> Map.<String, String>of(
                "role", t.getRole() == ChatRole.USER ? "user" : "assistant",
                "content", t.getText() != null ? t.getText() : ""
            ))
            .toList();
        String aiText = tourGuideAiService.generateResponse(tourContext, history);

        ChatTurn llmTurn = ChatTurn.create(session, nextIdx + 1, ChatRole.GUIDE, ChatSource.LLM, aiText, null, null);
        llmTurn = chatTurnRepository.save(llmTurn);

        return new com.app.questofseoul.dto.tour.SendMessageResponse(userTurn.getId(), text, llmTurn.getId(), aiText);
    }

    private String buildTourContextForAi(Long tourId, Long currentStepId) {
        Tour tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("투어: ").append(tour.getTitleEn()).append("\n");
        if (tour.getDescriptionEn() != null) sb.append("설명: ").append(tour.getDescriptionEn()).append("\n");
        List<Step> steps = stepRepository.findByTourIdOrderByStepOrderAsc(tourId);
        for (Step s : steps) {
            sb.append("- 스텝 ").append(s.getStepOrder()).append(": ").append(s.getTitleEn());
            if (s.getShortDescEn() != null) sb.append(" - ").append(s.getShortDescEn());
            sb.append("\n");
            GuideContent guide = guideContentRepository.findByStepId(s.getId()).orElse(null);
            if (guide != null) {
                for (GuideSegment gs : guide.getSegments().stream().sorted(Comparator.comparing(GuideSegment::getSegIdx)).toList()) {
                    if (gs.getTextEn() != null) sb.append("  가이드: ").append(gs.getTextEn()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public GuideSegmentResponse getStepGuide(Long stepId, Language lang) {
        Step step = stepRepository.findById(stepId).orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        GuideContent guide = guideContentRepository.findByStepId(stepId).orElse(null);
        if (guide == null) return new GuideSegmentResponse(step.getId(), step.getTitleEn(), List.of());

        List<GuideSegmentResponse.SegmentItem> items = guide.getSegments().stream()
            .sorted(Comparator.comparing(GuideSegment::getSegIdx))
            .map(seg -> {
                List<GuideSegmentResponse.AssetItem> media = seg.getMediaMaps().stream()
                    .sorted(Comparator.comparing(m -> m.getSortOrder() != null ? m.getSortOrder() : 0))
                    .map(m -> new GuideSegmentResponse.AssetItem(m.getMediaAsset().getId(), m.getMediaAsset().getUrlOrKey(), m.getMediaAsset().getMetaJson()))
                    .toList();
                return new GuideSegmentResponse.SegmentItem(seg.getId(), seg.getSegIdx(), seg.getTextEn(), seg.getTriggerKey(), media);
            })
            .toList();
        return new GuideSegmentResponse(step.getId(), step.getTitleEn(), items);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getStepQuizzes(Long stepId) {
        stepRepository.findById(stepId).orElseThrow(() -> new ResourceNotFoundException("Step not found"));
        return quizRepository.findByStepIdOrderByIdAsc(stepId).stream()
            .map(q -> new QuizResponse(q.getId(), q.getExternalKey(), q.getType(), q.getPromptEn(),
                q.getSpecJson(), q.getHintEn(), q.getMintReward(), q.getHintEn() != null && !q.getHintEn().isBlank()))
            .toList();
    }
}

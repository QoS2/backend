package com.app.questofseoul.service;

import com.app.questofseoul.config.AiServerProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TourGuideAiService {

    private static final Logger log = LoggerFactory.getLogger(TourGuideAiService.class);

    private final AiServerProperties aiServerProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateResponse(String tourContext, List<Map<String, String>> chatHistory) {
        if (!aiServerProperties.enabled() || aiServerProperties.baseUrl() == null || aiServerProperties.baseUrl().isBlank()) {
            return "AI 가이드가 비활성화되어 있습니다.";
        }

        try {
            String url = aiServerProperties.baseUrl().replaceAll("/$", "") + "/tour-guide/chat";
            Map<String, Object> body = Map.of(
                "tourContext", tourContext != null ? tourContext : "",
                "history", chatHistory != null ? chatHistory : List.of()
            );
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("AI server error: {} - {}", response.statusCode(), response.body());
                return "죄송합니다. 잠시 후 다시 질문해 주세요.";
            }

            JsonNode root = objectMapper.readTree(response.body());
            String text = root.path("text").asText("");
            return !text.isBlank() ? text : "답변을 생성할 수 없습니다.";
        } catch (Exception e) {
            log.error("AI server call failed", e);
            return "죄송합니다. AI 응답 생성 중 오류가 발생했습니다.";
        }
    }
}

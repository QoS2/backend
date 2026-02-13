package com.app.questofseoul.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private static final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private static final String MODEL = "text-embedding-3-small";
    private static final int DIMENSIONS = 1536;

    private final RestClient restClient;

    public EmbeddingService(@Value("${OPENAI_API_KEY:}") String apiKey) {
        boolean hasKey = apiKey != null && !apiKey.isBlank();
        this.restClient = hasKey ? RestClient.builder()
            .baseUrl(EMBEDDING_URL)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build() : null;
    }

    public float[] embed(String text) {
        if (text == null || text.isBlank() || restClient == null) {
            return new float[0];
        }
        String truncated = text.length() > 8000 ? text.substring(0, 8000) : text;
        Map<String, Object> request = Map.of(
            "model", MODEL,
            "input", truncated,
            "dimensions", DIMENSIONS
        );
        @SuppressWarnings("unchecked")
        var response = restClient.post()
            .body(request)
            .retrieve()
            .body(Map.class);
        if (response == null) return new float[0];
        @SuppressWarnings("unchecked")
        var data = (List<Map<String, Object>>) response.get("data");
        if (data == null || data.isEmpty()) return new float[0];
        @SuppressWarnings("unchecked")
        var embedding = (List<Number>) data.get(0).get("embedding");
        if (embedding == null) return new float[0];
        float[] result = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            result[i] = embedding.get(i).floatValue();
        }
        return result;
    }
}

package com.inboxiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClassifierService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> classifySender(String sender, int count, List<String> subjectSamples) {
        String prompt = buildPrompt(sender, count, subjectSamples);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            String response = webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            return parseGeminiResponse(response);
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("decision", "keep");
            fallback.put("confidence", 0.0);
            fallback.put("reason", "error: " + e.getMessage());
            return fallback;
        }
    }

    private String buildPrompt(String sender, int count, List<String> subjectSamples) {
        return String.format("""
            You are helping a user declutter their inbox. Given this email sender's data, decide if it looks like:
            - "keep" (important/personal/wanted subscription)
            - "unsubscribe" (spam, promotional, unwanted newsletter)

            Sender: %s
            Number of emails received: %d
            Sample subjects: %s

            Respond ONLY in valid JSON, no markdown, no code fences:
            {"decision": "keep" or "unsubscribe", "confidence": 0-1, "reason": "short reason"}
            """, sender, count, subjectSamples);
    }

    private Map<String, Object> parseGeminiResponse(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        String text = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        text = text.trim();
        if (text.startsWith("```")) {
            text = text.replaceAll("```json", "").replaceAll("```", "").trim();
        }

        JsonNode parsed = objectMapper.readTree(text);
        Map<String, Object> result = new HashMap<>();
        result.put("decision", parsed.path("decision").asText("keep"));
        result.put("confidence", parsed.path("confidence").asDouble(0.0));
        result.put("reason", parsed.path("reason").asText(""));
        return result;
    }
}

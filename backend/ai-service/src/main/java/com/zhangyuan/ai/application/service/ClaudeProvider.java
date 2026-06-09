package com.zhangyuan.ai.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.ai.domain.model.ChatRequest;
import com.zhangyuan.ai.domain.model.ChatResponse;
import com.zhangyuan.ai.domain.service.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ClaudeProvider implements ModelProvider {
    private static final Logger log = LoggerFactory.getLogger(ClaudeProvider.class);
    private static final List<String> SUPPORTED_MODELS = List.of(
            "claude-3-opus", "claude-3-sonnet", "claude-3-haiku", "claude-2");
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() { return "claude"; }

    @Override
    public boolean supportsModel(String model) {
        return SUPPORTED_MODELS.stream().anyMatch(m -> model.startsWith(m) || model.contains("claude"));
    }

    @Override
    public ChatResponse chat(ChatRequest request, Map<String, String> config) {
        try {
            String apiKey = config.get("api-key");
            String baseUrl = config.getOrDefault("base-url", "https://api.anthropic.com");

            String system = null;
            List<Map<String, String>> messages = new ArrayList<>();
            for (ChatRequest.Message msg : request.getMessages()) {
                if ("system".equals(msg.getRole())) {
                    system = msg.getContent();
                } else {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }
            }

            Map<String, Object> body = new java.util.LinkedHashMap<>();
            body.put("model", request.getModel());
            body.put("messages", messages);
            body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 1024);
            if (request.getTemperature() != null) body.put("temperature", request.getTemperature());
            if (system != null) body.put("system", system);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    baseUrl + "/v1/messages", entity, JsonNode.class);

            JsonNode json = response.getBody();
            if (json == null) throw new RuntimeException("Empty response from Claude");

            String id = json.get("id").asText();
            String model = json.get("model").asText();

            int inputTokens = json.path("usage").path("input_tokens").asInt(0);
            int outputTokens = json.path("usage").path("output_tokens").asInt(0);

            List<ChatResponse.Choice> choices = new ArrayList<>();
            ChatResponse.Choice choice = new ChatResponse.Choice();
            choice.setIndex(0);
            ChatResponse.Message msg = new ChatResponse.Message();
            msg.setRole("assistant");

            StringBuilder contentBuilder = new StringBuilder();
            for (JsonNode contentNode : json.get("content")) {
                if ("text".equals(contentNode.get("type").asText())) {
                    contentBuilder.append(contentNode.get("text").asText());
                }
            }
            msg.setContent(contentBuilder.toString());
            choice.setMessage(msg);
            choice.setFinishReason(json.path("stop_reason").asText("end_turn"));
            choices.add(choice);

            log.info("Claude call success: model={}, tokens={}+{}", model, inputTokens, outputTokens);
            return new ChatResponse(id, model, choices, new ChatResponse.Usage(inputTokens, outputTokens));
        } catch (Exception e) {
            log.error("Claude call failed: {}", e.getMessage());
            throw new RuntimeException("Claude API error: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> chatStream(ChatRequest request, Map<String, String> config) {
        return Flux.error(new UnsupportedOperationException("Streaming not yet implemented"));
    }

    @Override
    public int estimateTokens(String model, String text) {
        return (int) Math.ceil(text.length() / 3.5);
    }
}

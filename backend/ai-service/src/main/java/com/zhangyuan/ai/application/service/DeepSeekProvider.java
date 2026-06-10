package com.zhangyuan.ai.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.ai.domain.model.ChatRequest;
import com.zhangyuan.ai.domain.model.ChatResponse;
import com.zhangyuan.ai.domain.model.EmbeddingRequest;
import com.zhangyuan.ai.domain.model.EmbeddingResponse;
import com.zhangyuan.ai.domain.service.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekProvider implements ModelProvider {
    private static final Logger log = LoggerFactory.getLogger(DeepSeekProvider.class);
    private static final List<String> SUPPORTED_MODELS = List.of(
            "deepseek-chat", "deepseek-coder");
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() { return "deepseek"; }

    @Override
    public boolean supportsModel(String model) {
        return SUPPORTED_MODELS.stream().anyMatch(model::startsWith);
    }

    @Override
    public ChatResponse chat(ChatRequest request, Map<String, String> config) {
        try {
            String apiKey = config.get("api-key");
            String baseUrl = config.getOrDefault("base-url", "https://api.deepseek.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = objectMapper.convertValue(request, Map.class);
            body.remove("stream");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions", entity, JsonNode.class);

            JsonNode json = response.getBody();
            if (json == null) throw new RuntimeException("Empty response from DeepSeek");

            String id = json.get("id").asText();
            String model = json.get("model").asText();
            int promptTokens = json.path("usage").path("prompt_tokens").asInt(0);
            int completionTokens = json.path("usage").path("completion_tokens").asInt(0);

            List<ChatResponse.Choice> choices = new ArrayList<>();
            for (JsonNode choiceNode : json.get("choices")) {
                ChatResponse.Choice choice = new ChatResponse.Choice();
                choice.setIndex(choiceNode.get("index").asInt());
                ChatResponse.Message msg = new ChatResponse.Message();
                msg.setRole(choiceNode.path("message").path("role").asText("assistant"));
                msg.setContent(choiceNode.path("message").path("content").asText(""));
                choice.setMessage(msg);
                choice.setFinishReason(choiceNode.path("finish_reason").asText("stop"));
                choices.add(choice);
            }

            log.info("DeepSeek call success: model={}, tokens={}+{}", model, promptTokens, completionTokens);
            return new ChatResponse(id, model, choices, new ChatResponse.Usage(promptTokens, completionTokens));
        } catch (Exception e) {
            log.error("DeepSeek call failed: {}", e.getMessage());
            throw new RuntimeException("DeepSeek API error: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> chatStream(ChatRequest request, Map<String, String> config) {
        String apiKey = config.get("api-key");
        String baseUrl = config.getOrDefault("base-url", "https://api.deepseek.com");

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("DeepSeek API key not configured, returning mock streaming response");
            return createMockStream(request);
        }

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

        Map<String, Object> body = objectMapper.convertValue(request, Map.class);
        body.put("stream", true);

        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(chunk -> Flux.fromArray(chunk.split("\n")))
                .map(String::trim)
                .filter(line -> line.startsWith("data: "))
                .map(line -> line.substring(6).trim())
                .filter(data -> !"[DONE]".equals(data))
                .doOnError(e -> log.error("DeepSeek streaming error: {}", e.getMessage()));
    }

    private Flux<String> createMockStream(ChatRequest request) {
        String model = request.getModel();
        long now = System.currentTimeMillis() / 1000;
        return Flux.just(
                mockChunk(now, model, "assistant", null, null),
                mockChunk(now, model, null, "Hello! ", null),
                mockChunk(now, model, null, "This is a mock streaming response from DeepSeek.", null),
                mockChunk(now, model, null, null, "stop")
        ).delayElements(Duration.ofMillis(100));
    }

    private String mockChunk(long now, String model, String role, String content, String finishReason) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"chatcmpl-mock\",\"object\":\"chat.completion.chunk\",\"created\":")
                .append(now).append(",\"model\":\"").append(model).append("\",\"choices\":[{\"index\":0,\"delta\":{");
        if (role != null) sb.append("\"role\":\"").append(role).append("\"");
        if (content != null) sb.append("\"content\":\"").append(content).append("\"");
        sb.append("}");
        if (finishReason != null) sb.append(",\"finish_reason\":\"").append(finishReason).append("\"");
        else sb.append(",\"finish_reason\":null");
        sb.append("}]}");
        return sb.toString();
    }

    @Override
    public int estimateTokens(String model, String text) {
        return (int) Math.ceil(text.length() / 4.0);
    }
}

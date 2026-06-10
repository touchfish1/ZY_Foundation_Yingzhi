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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
        String apiKey = config.get("api-key");
        String baseUrl = config.getOrDefault("base-url", "https://api.anthropic.com");

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Claude API key not configured, returning mock streaming response");
            return createMockStream(request);
        }

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
        body.put("stream", true);
        if (request.getTemperature() != null) body.put("temperature", request.getTemperature());
        if (system != null) body.put("system", system);

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

        AtomicReference<String> messageIdRef = new AtomicReference<>("msg_unknown");

        return webClient.post()
                .uri("/v1/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(chunk -> Flux.fromArray(chunk.split("\n")))
                .bufferUntil(String::isEmpty)
                .filter(lines -> !lines.isEmpty())
                .map(lines -> {
                    String eventType = "";
                    String data = "";
                    for (String line : lines) {
                        if (line.startsWith("event: ")) {
                            eventType = line.substring(7).trim();
                        } else if (line.startsWith("data: ")) {
                            data = line.substring(6).trim();
                        }
                    }
                    return parseClaudeEvent(eventType, data, messageIdRef);
                })
                .filter(Objects::nonNull)
                .doOnError(e -> log.error("Claude streaming error: {}", e.getMessage()));
    }

    private String parseClaudeEvent(String eventType, String data,
                                    AtomicReference<String> messageIdRef) {
        if (data.isEmpty()) return null;

        try {
            JsonNode dataJson = objectMapper.readTree(data);
            String msgId = messageIdRef.get();
            String model = dataJson.path("message").path("model").asText(
                    dataJson.path("model").asText("unknown"));

            switch (eventType) {
                case "message_start": {
                    String id = dataJson.path("message").path("id").asText(null);
                    if (id != null) messageIdRef.set(id);
                    return null;
                }
                case "content_block_delta": {
                    String deltaType = dataJson.path("delta").path("type").asText("");
                    if ("text_delta".equals(deltaType)) {
                        String text = dataJson.path("delta").path("text").asText("");
                        if (text.isEmpty()) return null;
                        return buildChunkJson(msgId, model, text, null);
                    }
                    return null;
                }
                case "message_delta": {
                    String stopReason = dataJson.path("delta").path("stop_reason").asText(null);
                    if (stopReason != null) {
                        return buildChunkJson(msgId, model, null, mapStopReason(stopReason));
                    }
                    return null;
                }
                case "message_stop": {
                    return buildChunkJson(msgId, model, null, "stop");
                }
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Failed to parse Claude SSE event: {}", e.getMessage());
            return null;
        }
    }

    private String buildChunkJson(String id, String model, String content, String finishReason) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(id != null ? id : "msg_unknown").append("\",");
        sb.append("\"object\":\"chat.completion.chunk\",");
        sb.append("\"created\":").append(System.currentTimeMillis() / 1000).append(",");
        sb.append("\"model\":\"").append(model).append("\",");
        sb.append("\"choices\":[{\"index\":0,");
        sb.append("\"delta\":{");
        if (content != null) {
            sb.append("\"content\":\"").append(escapeJson(content)).append("\"");
        }
        sb.append("}");
        if (finishReason != null) {
            sb.append(",\"finish_reason\":\"").append(finishReason).append("\"");
        } else {
            sb.append(",\"finish_reason\":null");
        }
        sb.append("}]}");
        return sb.toString();
    }

    private static String mapStopReason(String claudeStopReason) {
        if (claudeStopReason == null) return "stop";
        return switch (claudeStopReason) {
            case "end_turn" -> "stop";
            case "max_tokens" -> "length";
            case "stop_sequence" -> "stop";
            case "tool_use" -> "tool_calls";
            default -> claudeStopReason;
        };
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Flux<String> createMockStream(ChatRequest request) {
        String model = request.getModel();
        long now = System.currentTimeMillis() / 1000;
        String mockChunk1 = "{\"id\":\"msg_mock\",\"object\":\"chat.completion.chunk\",\"created\":"
                + now + ",\"model\":\"" + model
                + "\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\"},\"finish_reason\":null}]}";
        String mockChunk2 = "{\"id\":\"msg_mock\",\"object\":\"chat.completion.chunk\",\"created\":"
                + now + ",\"model\":\"" + model
                + "\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello! \"},\"finish_reason\":null}]}";
        String mockChunk3 = "{\"id\":\"msg_mock\",\"object\":\"chat.completion.chunk\",\"created\":"
                + now + ",\"model\":\"" + model
                + "\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"This is a mock streaming response from Claude.\"},\"finish_reason\":null}]}";
        String mockChunk4 = "{\"id\":\"msg_mock\",\"object\":\"chat.completion.chunk\",\"created\":"
                + now + ",\"model\":\"" + model
                + "\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}";

        return Flux.just(mockChunk1, mockChunk2, mockChunk3, mockChunk4)
                .delayElements(Duration.ofMillis(100));
    }

    @Override
    public int estimateTokens(String model, String text) {
        return (int) Math.ceil(text.length() / 3.5);
    }
}

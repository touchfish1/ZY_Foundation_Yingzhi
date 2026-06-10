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
import java.util.Objects;

@Component
public class OpenAiProvider implements ModelProvider {
    private static final Logger log = LoggerFactory.getLogger(OpenAiProvider.class);
    private static final List<String> SUPPORTED_MODELS = List.of(
            "gpt-4", "gpt-4-turbo", "gpt-4o", "gpt-3.5-turbo", "text-embedding-3-small");
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() { return "openai"; }

    @Override
    public boolean supportsModel(String model) {
        return SUPPORTED_MODELS.stream().anyMatch(m -> model.startsWith(m) || model.contains("gpt") || model.contains("text-embedding"));
    }

    @Override
    public ChatResponse chat(ChatRequest request, Map<String, String> config) {
        try {
            String apiKey = config.get("api-key");
            String baseUrl = config.getOrDefault("base-url", "https://api.openai.com");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = objectMapper.convertValue(request, Map.class);
            body.remove("stream");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    baseUrl + "/v1/chat/completions", entity, JsonNode.class);

            JsonNode json = response.getBody();
            if (json == null) throw new RuntimeException("Empty response from OpenAI");

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

            log.info("OpenAI call success: model={}, tokens={}+{}", model, promptTokens, completionTokens);
            return new ChatResponse(id, model, choices, new ChatResponse.Usage(promptTokens, completionTokens));
        } catch (Exception e) {
            log.error("OpenAI call failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI API error: " + e.getMessage(), e);
        }
    }

    @Override
    public Flux<String> chatStream(ChatRequest request, Map<String, String> config) {
        String apiKey = config.get("api-key");
        String baseUrl = config.getOrDefault("base-url", "https://api.openai.com");

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API key not configured, returning mock streaming response");
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
                .doOnError(e -> log.error("OpenAI streaming error: {}", e.getMessage()));
    }

    private Flux<String> createMockStream(ChatRequest request) {
        String model = request.getModel();
        String mockChunk1 = "{\"id\":\"chatcmpl-mock\",\"object\":\"chat.completion.chunk\",\"created\":"
                + (System.currentTimeMillis() / 1000)
                + ",\"model\":\"" + model
                + "\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\"},\"finish_reason\":null}]}";
        String mockChunk2 = "{\"id\":\"chatcmpl-mock\",\"object\":\"chat.completion.chunk\",\"created\":"
                + (System.currentTimeMillis() / 1000)
                + ",\"model\":\"" + model
                + "\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello! \"},\"finish_reason\":null}]}";
        String mockChunk3 = "{\"id\":\"chatcmpl-mock\",\"object\":\"chat.completion.chunk\",\"created\":"
                + (System.currentTimeMillis() / 1000)
                + ",\"model\":\"" + model
                + "\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"This is a mock streaming response from OpenAI.\"},\"finish_reason\":null}]}";
        String mockChunk4 = "{\"id\":\"chatcmpl-mock\",\"object\":\"chat.completion.chunk\",\"created\":"
                + (System.currentTimeMillis() / 1000)
                + ",\"model\":\"" + model
                + "\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}]}";

        return Flux.just(mockChunk1, mockChunk2, mockChunk3, mockChunk4)
                .delayElements(Duration.ofMillis(100));
    }

    public EmbeddingResponse embed(EmbeddingRequest request, Map<String, String> config) {
        String apiKey = config.get("api-key");
        String baseUrl = config.getOrDefault("base-url", "https://api.openai.com");

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("OpenAI API key not configured, returning mock embedding response");
            return createMockEmbedding(request);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", request.getModel());
            body.put("input", request.getInput());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    baseUrl + "/v1/embeddings", entity, JsonNode.class);

            JsonNode json = response.getBody();
            if (json == null) throw new RuntimeException("Empty response from OpenAI embeddings API");

            String model = json.get("model").asText();
            int promptTokens = json.path("usage").path("prompt_tokens").asInt(0);
            int totalTokens = json.path("usage").path("total_tokens").asInt(0);

            List<EmbeddingResponse.EmbeddingData> dataList = new ArrayList<>();
            for (JsonNode dataNode : json.get("data")) {
                List<Double> embedding = new ArrayList<>();
                for (JsonNode val : dataNode.get("embedding")) {
                    embedding.add(val.asDouble());
                }
                dataList.add(new EmbeddingResponse.EmbeddingData(
                        dataNode.get("index").asInt(), embedding));
            }

            log.info("OpenAI embedding success: model={}, tokens={}", model, totalTokens);
            return new EmbeddingResponse(model, dataList,
                    new EmbeddingResponse.Usage(promptTokens, totalTokens));
        } catch (Exception e) {
            log.error("OpenAI embedding call failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI embedding API error: " + e.getMessage(), e);
        }
    }

    private EmbeddingResponse createMockEmbedding(EmbeddingRequest request) {
        int dimension = 1536;
        List<String> inputs = request.getInput();
        if (inputs == null || inputs.isEmpty()) {
            inputs = List.of("");
        }

        List<EmbeddingResponse.EmbeddingData> dataList = new ArrayList<>();
        java.util.Random rand = new java.util.Random(42);
        for (int i = 0; i < inputs.size(); i++) {
            List<Double> embedding = new ArrayList<>(dimension);
            for (int j = 0; j < dimension; j++) {
                embedding.add(rand.nextGaussian() * 0.01);
            }
            dataList.add(new EmbeddingResponse.EmbeddingData(i, embedding));
        }

        int totalTokens = inputs.stream()
                .mapToInt(s -> (int) Math.ceil(s.length() / 4.0))
                .sum();
        return new EmbeddingResponse(request.getModel(), dataList,
                new EmbeddingResponse.Usage(totalTokens, totalTokens));
    }

    @Override
    public int estimateTokens(String model, String text) {
        return (int) Math.ceil(text.length() / 4.0);
    }
}

package com.zhangyuan.ai.adapter.in.rest;

import com.zhangyuan.ai.application.service.OpenAiProvider;
import com.zhangyuan.ai.application.service.ProviderRegistry;
import com.zhangyuan.ai.client.OrderServiceClient;
import com.zhangyuan.ai.client.UserServiceClient;
import com.zhangyuan.ai.domain.model.EmbeddingRequest;
import com.zhangyuan.ai.domain.model.EmbeddingResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class EmbeddingController {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingController.class);

    private final OpenAiProvider openAiProvider;
    private final ProviderRegistry providerRegistry;
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;

    public EmbeddingController(OpenAiProvider openAiProvider,
                                ProviderRegistry providerRegistry,
                                UserServiceClient userServiceClient,
                                OrderServiceClient orderServiceClient) {
        this.openAiProvider = openAiProvider;
        this.providerRegistry = providerRegistry;
        this.userServiceClient = userServiceClient;
        this.orderServiceClient = orderServiceClient;
    }

    @PostMapping("/embeddings")
    public ResponseEntity<?> embed(
            @Valid @RequestBody EmbeddingRequest request,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", Map.of("message", "Missing API key", "type", "auth_error")));
        }

        String apiKey = authHeader.substring(7);

        try {
            Map<String, Object> userQuota = verifyKey(apiKey);
            Long userId = ((Number) userQuota.get("userId")).longValue();
            long quotaUsed = userQuota.get("quotaUsed") != null
                    ? ((Number) userQuota.get("quotaUsed")).longValue() : 0;
            long quotaLimit = userQuota.get("quotaLimit") != null
                    ? ((Number) userQuota.get("quotaLimit")).longValue() : 0;

            if (quotaLimit > 0 && quotaUsed >= quotaLimit) {
                throw new IllegalStateException("Quota exhausted");
            }

            Map<String, String> config = providerRegistry.getConfig("openai");

            long startMs = System.currentTimeMillis();
            EmbeddingResponse response = openAiProvider.embed(request, config);
            long durationMs = System.currentTimeMillis() - startMs;

            recordUsage(userId, apiKey, request, response, durationMs);

            log.info("Embedding completed: userId={}, model={}, tokens={}, duration={}ms",
                    userId, response.getModel(),
                    response.getUsage() != null ? response.getUsage().getTotalTokens() : 0,
                    durationMs);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", Map.of("message", e.getMessage(), "type", "auth_error")));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", Map.of("message", e.getMessage(), "type", "quota_exceeded")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", Map.of("message", e.getMessage(), "type", "invalid_request")));
        } catch (Exception e) {
            log.error("Embedding error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", Map.of("message", "Internal server error", "type", "server_error")));
        }
    }

    private Map<String, Object> verifyKey(String apiKey) {
        var resp = userServiceClient.verifyApiKey(apiKey);
        if (resp == null || resp.code() != 0 || resp.data() == null) {
            throw new SecurityException("Invalid API key");
        }
        return resp.data();
    }

    private void recordUsage(Long userId, String apiKey, EmbeddingRequest request,
                             EmbeddingResponse response, long durationMs) {
        try {
            int totalTokens = response.getUsage() != null ? response.getUsage().getTotalTokens() : 0;
            Map<String, Object> record = Map.of(
                    "userId", userId,
                    "apiKey", apiKey,
                    "apiPath", "/v1/embeddings",
                    "model", request.getModel(),
                    "tokensIn", totalTokens,
                    "tokensOut", 0,
                    "cost", BigDecimal.ZERO,
                    "durationMs", (int) durationMs,
                    "status", "SUCCESS"
            );
            orderServiceClient.recordUsage(record);
        } catch (Exception e) {
            log.warn("Failed to record usage for userId={}: {}", userId, e.getMessage());
        }
    }
}

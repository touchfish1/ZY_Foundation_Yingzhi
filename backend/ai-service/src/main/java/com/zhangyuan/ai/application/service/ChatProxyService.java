package com.zhangyuan.ai.application.service;

import com.zhangyuan.ai.client.OrderServiceClient;
import com.zhangyuan.ai.client.UserServiceClient;
import com.zhangyuan.ai.domain.model.ChatRequest;
import com.zhangyuan.ai.domain.model.ChatResponse;
import com.zhangyuan.ai.domain.service.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class ChatProxyService {
    private static final Logger log = LoggerFactory.getLogger(ChatProxyService.class);

    private final ProviderRegistry providerRegistry;
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;

    public ChatProxyService(ProviderRegistry providerRegistry,
                            UserServiceClient userServiceClient,
                            OrderServiceClient orderServiceClient) {
        this.providerRegistry = providerRegistry;
        this.userServiceClient = userServiceClient;
        this.orderServiceClient = orderServiceClient;
    }

    public ChatResponse chat(String apiKey, ChatRequest request) {
        Map<String, Object> userQuota = verifyKey(apiKey);
        Long userId = ((Number) userQuota.get("userId")).longValue();
        long quotaUsed = userQuota.get("quotaUsed") != null ? ((Number) userQuota.get("quotaUsed")).longValue() : 0;
        long quotaLimit = userQuota.get("quotaLimit") != null ? ((Number) userQuota.get("quotaLimit")).longValue() : 0;

        if (quotaLimit > 0 && quotaUsed >= quotaLimit) {
            throw new IllegalStateException("Quota exhausted");
        }

        ModelProvider provider = providerRegistry.resolveProvider(request.getModel());
        Map<String, String> config = providerRegistry.getConfig(provider.getName());

        int estimatedInputTokens = request.getMessages().stream()
                .mapToInt(m -> provider.estimateTokens(request.getModel(), m.getContent()))
                .sum();

        long startMs = System.currentTimeMillis();
        ChatResponse response = provider.chat(request, config);
        long durationMs = System.currentTimeMillis() - startMs;

        recordUsage(userId, apiKey, request, response, estimatedInputTokens, durationMs);

        log.info("Chat completed: userId={}, model={}, tokens={}+{}, duration={}ms",
                userId, response.getModel(),
                response.getUsage().getPromptTokens(),
                response.getUsage().getCompletionTokens(),
                durationMs);

        return response;
    }

    private Map<String, Object> verifyKey(String apiKey) {
        var resp = userServiceClient.verifyApiKey(apiKey);
        if (resp == null || resp.code() != 0 || resp.data() == null) {
            throw new SecurityException("Invalid API key");
        }
        return resp.data();
    }

    private void recordUsage(Long userId, String apiKey, ChatRequest request,
                             ChatResponse response, int estimatedInputTokens, long durationMs) {
        try {
            Map<String, Object> record = Map.of(
                    "userId", userId,
                    "apiKey", apiKey,
                    "apiPath", "/v1/chat/completions",
                    "model", request.getModel(),
                    "tokensIn", response.getUsage().getPromptTokens(),
                    "tokensOut", response.getUsage().getCompletionTokens(),
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

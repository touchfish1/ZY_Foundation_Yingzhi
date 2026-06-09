package com.zhangyuan.ai.domain.service;

import com.zhangyuan.ai.domain.model.ChatRequest;
import com.zhangyuan.ai.domain.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.Map;

public interface ModelProvider {
    String getName();
    ChatResponse chat(ChatRequest request, Map<String, String> providerConfig);
    Flux<String> chatStream(ChatRequest request, Map<String, String> providerConfig);
    boolean supportsModel(String model);
    int estimateTokens(String model, String text);
}

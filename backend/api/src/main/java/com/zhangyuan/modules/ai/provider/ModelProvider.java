package com.zhangyuan.modules.ai.provider;

import java.util.List;

/**
 * Abstraction for AI model providers (OpenAI, Anthropic, local models, etc.).
 * <p>
 * NOTE: When spring-boot-starter-webflux is added to the project dependencies,
 * the return types below should be changed to use {@code reactor.core.publisher.Flux}
 * for streaming responses:
 * <pre>{@code
 * import reactor.core.publisher.Flux;
 * Flux<StreamChunk> chatCompletionStream(ModelCompletionRequest request);
 * }</pre>
 */
public interface ModelProvider {

    String getProviderName();

    boolean supportsModel(String modelName);

    /**
     * Non-streaming chat completion.
     */
    ModelCompletionResponse chatCompletion(ModelCompletionRequest request);

    /**
     * Streaming chat completion (SSE chunks).
     * Returns a list of chunks; switch to {@code Flux<StreamChunk>} once WebFlux is available.
     */
    List<StreamChunk> chatCompletionStream(ModelCompletionRequest request);

    /**
     * Embedding.
     */
    ModelEmbeddingResponse embed(ModelEmbeddingRequest request);
}

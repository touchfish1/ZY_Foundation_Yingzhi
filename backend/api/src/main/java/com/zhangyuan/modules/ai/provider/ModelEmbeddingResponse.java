package com.zhangyuan.modules.ai.provider;

import java.util.List;

public record ModelEmbeddingResponse(
    String model,
    List<Embedding> data,
    Usage usage
) {
    public record Embedding(List<Double> embedding, int index) {}
    public record Usage(int promptTokens, int totalTokens) {}
}

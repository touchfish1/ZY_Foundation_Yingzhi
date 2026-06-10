package com.zhangyuan.modules.ai.provider;

import java.util.List;

public record ModelEmbeddingRequest(
    String model,
    List<String> input
) {}

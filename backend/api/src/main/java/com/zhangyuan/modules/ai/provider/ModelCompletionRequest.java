package com.zhangyuan.modules.ai.provider;

import java.util.List;

public record ModelCompletionRequest(
    String model,
    List<Message> messages,
    Double temperature,
    Integer maxTokens,
    Boolean stream,
    // Additional OpenAI-compatible params
    Double topP,
    List<String> stop,
    Double presencePenalty,
    Double frequencyPenalty
) {
    public record Message(String role, String content) {}
}

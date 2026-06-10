package com.zhangyuan.modules.ai.provider;

import java.util.List;

public record ModelCompletionResponse(
    String id,
    String model,
    List<Choice> choices,
    Usage usage
) {
    public record Choice(int index, Message message, String finishReason) {}
    public record Message(String role, String content) {}
    public record Usage(int promptTokens, int completionTokens, int totalTokens) {}
}

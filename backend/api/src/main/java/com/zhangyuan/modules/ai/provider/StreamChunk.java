package com.zhangyuan.modules.ai.provider;

import java.util.List;

public record StreamChunk(
    String id,
    String model,
    List<Choice> choices
) {
    public record Choice(int index, Delta delta, String finishReason) {}
    public record Delta(String role, String content) {}
}

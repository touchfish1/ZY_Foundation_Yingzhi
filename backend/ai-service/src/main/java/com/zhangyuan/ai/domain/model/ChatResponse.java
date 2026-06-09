package com.zhangyuan.ai.domain.model;

import java.util.List;

public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    public ChatResponse() {}
    public ChatResponse(String id, String model, List<Choice> choices, Usage usage) {
        this.id = id;
        this.object = "chat.completion";
        this.created = System.currentTimeMillis() / 1000;
        this.model = model;
        this.choices = choices;
        this.usage = usage;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getObject() { return object; }
    public long getCreated() { return created; }
    public String getModel() { return model; }
    public List<Choice> getChoices() { return choices; }
    public Usage getUsage() { return usage; }

    public static class Choice {
        private int index;
        private Message message;
        private String finishReason;

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
    }

    public static class Message {
        private String role;
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;

        public Usage() {}
        public Usage(int promptTokens, int completionTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = promptTokens + completionTokens;
        }

        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    }
}

package com.zhangyuan.ai.domain.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ChatRequest {
    @NotBlank private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;
    private boolean stream;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }

    public static class Message {
        @NotBlank private String role;
        @NotBlank private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}

package com.zhangyuan.ai.domain.model;

import java.util.List;

public class EmbeddingResponse {
    private String object;
    private List<EmbeddingData> data;
    private String model;
    private Usage usage;

    public EmbeddingResponse() {}

    public EmbeddingResponse(String model, List<EmbeddingData> data, Usage usage) {
        this.object = "list";
        this.model = model;
        this.data = data;
        this.usage = usage;
    }

    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    public List<EmbeddingData> getData() { return data; }
    public void setData(List<EmbeddingData> data) { this.data = data; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }

    public static class EmbeddingData {
        private int index;
        private String object;
        private List<Double> embedding;

        public EmbeddingData() {}

        public EmbeddingData(int index, List<Double> embedding) {
            this.index = index;
            this.object = "embedding";
            this.embedding = embedding;
        }

        public int getIndex() { return index; }
        public void setIndex(int index) { this.index = index; }
        public String getObject() { return object; }
        public void setObject(String object) { this.object = object; }
        public List<Double> getEmbedding() { return embedding; }
        public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
    }

    public static class Usage {
        private int promptTokens;
        private int totalTokens;

        public Usage() {}

        public Usage(int promptTokens, int totalTokens) {
            this.promptTokens = promptTokens;
            this.totalTokens = totalTokens;
        }

        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    }
}

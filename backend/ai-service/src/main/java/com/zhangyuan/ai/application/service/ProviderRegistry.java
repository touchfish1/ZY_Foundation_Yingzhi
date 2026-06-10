package com.zhangyuan.ai.application.service;

import com.zhangyuan.ai.domain.service.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProviderRegistry {
    private static final Logger log = LoggerFactory.getLogger(ProviderRegistry.class);
    private final Map<String, ModelProvider> providerMap = new HashMap<>();
    private final Map<String, String> configMap;

    public ProviderRegistry(List<ModelProvider> providers,
                            org.springframework.core.env.Environment env) {
        for (ModelProvider provider : providers) {
            providerMap.put(provider.getName(), provider);
            log.info("Registered model provider: {}", provider.getName());
        }

        configMap = new HashMap<>();
        configMap.put("openai.api-key",
                env.getProperty("ai.providers.openai.api-key", ""));
        configMap.put("openai.base-url",
                env.getProperty("ai.providers.openai.base-url", "https://api.openai.com"));
        configMap.put("claude.api-key",
                env.getProperty("ai.providers.claude.api-key", ""));
        configMap.put("claude.base-url",
                env.getProperty("ai.providers.claude.base-url", "https://api.anthropic.com"));
        configMap.put("deepseek.api-key",
                env.getProperty("ai.providers.deepseek.api-key", ""));
        configMap.put("deepseek.base-url",
                env.getProperty("ai.providers.deepseek.base-url", "https://api.deepseek.com"));
        configMap.put("qwen.api-key",
                env.getProperty("ai.providers.qwen.api-key", ""));
        configMap.put("qwen.base-url",
                env.getProperty("ai.providers.qwen.base-url", "https://dashscope.aliyuncs.com"));
        configMap.put("moonshot.api-key",
                env.getProperty("ai.providers.moonshot.api-key", ""));
        configMap.put("moonshot.base-url",
                env.getProperty("ai.providers.moonshot.base-url", "https://api.moonshot.cn"));
    }

    public ModelProvider resolveProvider(String model) {
        for (ModelProvider provider : providerMap.values()) {
            if (provider.supportsModel(model)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unsupported model: " + model);
    }

    public Map<String, String> getConfig(String providerName) {
        return Map.of(
                "api-key", configMap.getOrDefault(providerName + ".api-key", ""),
                "base-url", configMap.getOrDefault(providerName + ".base-url", "")
        );
    }
}

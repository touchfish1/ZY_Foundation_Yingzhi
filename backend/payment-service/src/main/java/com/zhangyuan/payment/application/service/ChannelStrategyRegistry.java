package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChannelStrategyRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChannelStrategyRegistry.class);
    private final Map<String, PaymentChannelStrategy> strategyMap = new HashMap<>();

    public ChannelStrategyRegistry(List<PaymentChannelStrategy> strategies) {
        for (PaymentChannelStrategy strategy : strategies) {
            String channel = strategy.getChannel();
            if (strategyMap.containsKey(channel)) {
                log.warn("Duplicate channel strategy: {}, overwriting", channel);
            }
            strategyMap.put(channel, strategy);
            log.info("Registered payment channel strategy: {}", channel);
        }
    }

    public PaymentChannelStrategy getStrategy(String channel) {
        PaymentChannelStrategy strategy = strategyMap.get(channel);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment channel: " + channel);
        }
        return strategy;
    }

    public boolean hasStrategy(String channel) {
        return strategyMap.containsKey(channel);
    }

    public Map<String, PaymentChannelStrategy> getAll() {
        return Map.copyOf(strategyMap);
    }
}

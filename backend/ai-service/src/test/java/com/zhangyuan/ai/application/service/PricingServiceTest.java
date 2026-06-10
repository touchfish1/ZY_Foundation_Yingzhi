package com.zhangyuan.ai.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }

    @Test
    void calculateCost_knownModel_returnsCorrectCost() {
        // gpt-4o: input 5.00/1M tokens, output 15.00/1M tokens
        // 1000 prompt tokens + 200 completion tokens
        BigDecimal cost = pricingService.calculateCost("gpt-4o", 1000, 200);

        // inputCost = (1000 / 1_000_000) * 5.00 = 0.005
        // outputCost = (200 / 1_000_000) * 15.00 = 0.003
        // total = 0.00800000
        assertThat(cost).isEqualByComparingTo(new BigDecimal("0.00800000"));
        assertThat(cost.scale()).isEqualTo(8);
    }

    @Test
    void calculateCost_unknownModel_usesDefaultPrices() {
        // Default: input 1.00/1M, output 2.00/1M
        // 2000 prompt + 1000 completion
        BigDecimal cost = pricingService.calculateCost("custom-model", 2000, 1000);

        // inputCost = (2000 / 1_000_000) * 1.00 = 0.002
        // outputCost = (1000 / 1_000_000) * 2.00 = 0.002
        // total = 0.00400000
        assertThat(cost).isEqualByComparingTo(new BigDecimal("0.00400000"));
    }

    @Test
    void calculateCost_zeroTokens_returnsZero() {
        BigDecimal cost = pricingService.calculateCost("gpt-4o", 0, 0);
        assertThat(cost).isEqualByComparingTo(BigDecimal.ZERO.setScale(8));
    }

    @Test
    void calculateCost_onlyPromptTokens_returnsInputCostOnly() {
        // gpt-4o-mini: input 0.15/1M, output 0.60/1M
        // 5000 prompt + 0 completion
        BigDecimal cost = pricingService.calculateCost("gpt-4o-mini", 5000, 0);

        // inputCost = (5000 / 1_000_000) * 0.15 = 0.00075
        // outputCost = 0
        // total = 0.00075000
        assertThat(cost).isEqualByComparingTo(new BigDecimal("0.00075000"));
    }

    @Test
    void calculateCost_claude3Opus_returnsCorrectCost() {
        // claude-3-opus: input 15.00/1M, output 75.00/1M
        // 100 prompt + 50 completion
        BigDecimal cost = pricingService.calculateCost("claude-3-opus", 100, 50);

        // inputCost = (100 / 1_000_000) * 15.00 = 0.0015
        // outputCost = (50 / 1_000_000) * 75.00 = 0.00375
        // total = 0.00525000
        assertThat(cost).isEqualByComparingTo(new BigDecimal("0.00525000"));
    }

    @Test
    void calculateCost_largeTokenCount_handlesPrecision() {
        // 1_000_000 prompt tokens = exactly 1M tokens
        BigDecimal cost = pricingService.calculateCost("gpt-3.5-turbo", 1_000_000, 500_000);

        // inputCost = (1_000_000 / 1_000_000) * 0.50 = 0.50
        // outputCost = (500_000 / 1_000_000) * 1.50 = 0.75
        // total = 1.25000000
        assertThat(cost).isEqualByComparingTo(new BigDecimal("1.25000000"));
    }
}

package com.zhangyuan.ai.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for calculating AI model usage costs based on token counts and model pricing.
 * <p>
 * Pricing data is sourced from a hardcoded map matching the seed data in V011__create_model_config.sql.
 * <p>
 * TODO: Future enhancement — replace hardcoded map with a Feign client fetching from the api module's
 * {@code /admin/ai/pricing} endpoint (backed by the {@code ai_model_pricing} table).
 * The api module already has {@code ModelPricingConfig} entity and {@code ModelPricingAdminController}.
 * A Feign client interface like:
 * <pre>{@code
 * @FeignClient(name = "api", path = "/admin/ai/pricing")
 * public interface ModelPricingClient {
 *     @GetMapping("/{modelName}")
 *     ApiResponse<ModelPricingConfig> getPricing(@PathVariable("modelName") String modelName);
 * }
 * }</pre>
 * would allow PricingService to call the API and fall back to this hardcoded map on failure.
 */
@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    /**
     * Hardcoded pricing map: model_name -> [input_price_per_1M_tokens, output_price_per_1M_tokens].
     * Prices are in CNY (¥), matching seed data in V011 migration.
     * <p>
     * When a model is not found in this map, default prices are used as a fallback.
     */
    private static final Map<String, BigDecimal[]> PRICING_MAP = createPricingMap();

    private static final BigDecimal DEFAULT_INPUT_PRICE = new BigDecimal("1.00");
    private static final BigDecimal DEFAULT_OUTPUT_PRICE = new BigDecimal("2.00");
    private static final BigDecimal ONE_MILLION = new BigDecimal("1_000_000");
    private static final int COST_SCALE = 8;

    private static Map<String, BigDecimal[]> createPricingMap() {
        Map<String, BigDecimal[]> map = new HashMap<>();
        map.put("gpt-4o",                new BigDecimal[]{new BigDecimal("5.00"),   new BigDecimal("15.00")});
        map.put("gpt-4o-mini",           new BigDecimal[]{new BigDecimal("0.15"),   new BigDecimal("0.60")});
        map.put("gpt-4-turbo",           new BigDecimal[]{new BigDecimal("10.00"),  new BigDecimal("30.00")});
        map.put("gpt-3.5-turbo",         new BigDecimal[]{new BigDecimal("0.50"),   new BigDecimal("1.50")});
        map.put("claude-3-opus",         new BigDecimal[]{new BigDecimal("15.00"),  new BigDecimal("75.00")});
        map.put("claude-3-sonnet",       new BigDecimal[]{new BigDecimal("3.00"),   new BigDecimal("15.00")});
        map.put("claude-3-haiku",        new BigDecimal[]{new BigDecimal("0.25"),   new BigDecimal("1.25")});
        map.put("text-embedding-3-small", new BigDecimal[]{new BigDecimal("0.02"),  new BigDecimal("0.02")});
        return Collections.unmodifiableMap(map);
    }

    /**
     * Calculate the cost of a model invocation based on token usage.
     * <p>
     * Formula: cost = (promptTokens / 1_000_000 × inputPrice) + (completionTokens / 1_000_000 × outputPrice)
     * <p>
     * Intermediate division uses scale 12 to avoid precision loss before final rounding to scale 8.
     *
     * @param modelName       the model identifier (e.g. "gpt-4o")
     * @param promptTokens    number of input/prompt tokens
     * @param completionTokens number of output/completion tokens
     * @return calculated cost with scale 8, rounded half-up
     */
    public BigDecimal calculateCost(String modelName, int promptTokens, int completionTokens) {
        BigDecimal[] prices = PRICING_MAP.getOrDefault(modelName,
                new BigDecimal[]{DEFAULT_INPUT_PRICE, DEFAULT_OUTPUT_PRICE});

        BigDecimal inputPrice = prices[0];
        BigDecimal outputPrice = prices[1];

        BigDecimal inputCost = BigDecimal.valueOf(promptTokens)
                .divide(ONE_MILLION, 12, RoundingMode.HALF_UP)
                .multiply(inputPrice);

        BigDecimal outputCost = BigDecimal.valueOf(completionTokens)
                .divide(ONE_MILLION, 12, RoundingMode.HALF_UP)
                .multiply(outputPrice);

        BigDecimal totalCost = inputCost.add(outputCost)
                .setScale(COST_SCALE, RoundingMode.HALF_UP);

        if (log.isDebugEnabled()) {
            log.debug("Cost calculated: model={}, promptTokens={}, completionTokens={}, cost={}",
                    modelName, promptTokens, completionTokens, totalCost);
        }

        return totalCost;
    }
}

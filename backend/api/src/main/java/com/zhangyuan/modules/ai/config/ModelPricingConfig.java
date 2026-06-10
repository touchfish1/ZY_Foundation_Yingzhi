package com.zhangyuan.modules.ai.config;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ai_model_pricing")
public class ModelPricingConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, unique = true, length = 128)
    private String modelName;

    @Column(name = "input_price", nullable = false, precision = 12, scale = 8)
    private BigDecimal inputPrice = BigDecimal.ZERO;

    @Column(name = "output_price", nullable = false, precision = 12, scale = 8)
    private BigDecimal outputPrice = BigDecimal.ZERO;

    @Column(nullable = false, length = 8)
    private String currency = "CNY";

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom = Instant.now();

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ModelPricingConfig() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public BigDecimal getInputPrice() { return inputPrice; }
    public void setInputPrice(BigDecimal inputPrice) { this.inputPrice = inputPrice; }
    public BigDecimal getOutputPrice() { return outputPrice; }
    public void setOutputPrice(BigDecimal outputPrice) { this.outputPrice = outputPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Instant effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public Instant getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(Instant effectiveTo) { this.effectiveTo = effectiveTo; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

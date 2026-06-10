package com.zhangyuan.modules.ai.config;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_model_route")
public class ModelRouteConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, unique = true, length = 128)
    private String modelName;

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(name = "provider_model_name", nullable = false, length = 255)
    private String providerModelName;

    @Column(name = "model_type", nullable = false, length = 32)
    private String modelType = "chat";

    @Column(nullable = false, length = 16)
    private String status = "active";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ModelRouteConfig() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderModelName() { return providerModelName; }
    public void setProviderModelName(String providerModelName) { this.providerModelName = providerModelName; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

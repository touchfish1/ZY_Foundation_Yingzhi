package com.zhangyuan.modules.ai.config;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_plan_model_access", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"plan_code", "model_name"})
})
public class PlanModelAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_code", nullable = false, length = 64)
    private String planCode;

    @Column(name = "model_name", nullable = false, length = 128)
    private String modelName;

    @Column(name = "max_concurrency", nullable = false)
    private int maxConcurrency = 5;

    @Column(name = "max_rpm", nullable = false)
    private int maxRpm = 0;

    public PlanModelAccess() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public int getMaxConcurrency() { return maxConcurrency; }
    public void setMaxConcurrency(int maxConcurrency) { this.maxConcurrency = maxConcurrency; }
    public int getMaxRpm() { return maxRpm; }
    public void setMaxRpm(int maxRpm) { this.maxRpm = maxRpm; }
}

package com.zhangyuan.order.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "usage_record")
public class UsageRecordEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "api_key", length = 64)
    private String apiKey;
    @Column(name = "api_path")
    private String apiPath;
    @Column(name = "tokens_in")
    private Long tokensIn = 0L;
    @Column(name = "tokens_out")
    private Long tokensOut = 0L;
    @Column(precision = 20, scale = 8)
    private BigDecimal cost = BigDecimal.ZERO;
    @Column(name = "duration_ms")
    private Integer durationMs = 0;
    @Column(length = 16)
    private String status = "success";
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist public void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public UsageRecordEntity() {}

    public UsageRecordEntity(Long userId, String apiKey, String apiPath, Long tokensIn, Long tokensOut, BigDecimal cost, Integer durationMs, String status) {
        this.userId = userId; this.apiKey = apiKey; this.apiPath = apiPath;
        this.tokensIn = tokensIn; this.tokensOut = tokensOut; this.cost = cost;
        this.durationMs = durationMs; this.status = status;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getApiKey() { return apiKey; }
    public String getApiPath() { return apiPath; }
    public Long getTokensIn() { return tokensIn; }
    public Long getTokensOut() { return tokensOut; }
    public BigDecimal getCost() { return cost; }
    public Integer getDurationMs() { return durationMs; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}

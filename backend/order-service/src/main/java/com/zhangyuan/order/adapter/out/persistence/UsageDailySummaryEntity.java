package com.zhangyuan.order.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "usage_daily_summary", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
public class UsageDailySummaryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false)
    private LocalDate date;
    @Column(name = "total_calls")
    private Integer totalCalls = 0;
    @Column(name = "total_tokens_in")
    private Long totalTokensIn = 0L;
    @Column(name = "total_tokens_out")
    private Long totalTokensOut = 0L;
    @Column(name = "total_cost", precision = 20, scale = 8)
    private BigDecimal totalCost = BigDecimal.ZERO;

    public UsageDailySummaryEntity() {}
    public UsageDailySummaryEntity(Long userId, LocalDate date) { this.userId = userId; this.date = date; }

    // Getters and setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalDate getDate() { return date; }
    public Integer getTotalCalls() { return totalCalls; }
    public void setTotalCalls(Integer totalCalls) { this.totalCalls = totalCalls; }
    public Long getTotalTokensIn() { return totalTokensIn; }
    public void setTotalTokensIn(Long totalTokensIn) { this.totalTokensIn = totalTokensIn; }
    public Long getTotalTokensOut() { return totalTokensOut; }
    public void setTotalTokensOut(Long totalTokensOut) { this.totalTokensOut = totalTokensOut; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
}

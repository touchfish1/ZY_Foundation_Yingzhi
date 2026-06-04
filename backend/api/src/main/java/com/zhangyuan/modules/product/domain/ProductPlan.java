package com.zhangyuan.modules.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "product_plan")
public class ProductPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    private String description;

    @Column(length = 64)
    private String badge;

    @Column(nullable = false, length = 32)
    private String status = "enabled";

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected ProductPlan() {
    }

    public ProductPlan(Long groupId, String code, String name, String description, String badge, Integer sortOrder) {
        this.groupId = groupId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.badge = badge;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBadge() {
        return badge;
    }

    public String getStatus() {
        return status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}

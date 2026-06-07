package com.zhangyuan.modules.product.domain.model;

import com.zhangyuan.common.dddframework.AggregateRoot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanGroup extends AggregateRoot<Long> {

    private String code;
    private String name;
    private String description;
    private boolean enabled;
    private int sortOrder;
    private List<Plan> plans;
    private Instant createdAt;
    private Instant updatedAt;

    public PlanGroup(String code, String name, String description, int sortOrder) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
        this.enabled = true;
        this.plans = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateInfo(String name, String description, int sortOrder) {
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    public Plan addPlan(String code, String name, String description, String badge, int sortOrder) {
        Plan plan = new Plan(this, code, name, description, badge, sortOrder);
        plans.add(plan);
        this.updatedAt = Instant.now();
        return plan;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }
    public int getSortOrder() { return sortOrder; }
    public List<Plan> getPlans() { return Collections.unmodifiableList(plans); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

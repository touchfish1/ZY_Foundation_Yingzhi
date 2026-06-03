package com.zhangyuan.modules.product.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class PlanGroupTest {

    @Test
    void createGroup() {
        PlanGroup group = new PlanGroup("api_plans", "API Plans", "Description", 10);
        assertThat(group.getCode()).isEqualTo("api_plans");
        assertThat(group.isEnabled()).isTrue();
    }

    @Test
    void addPlan() {
        PlanGroup group = new PlanGroup("api_plans", "API Plans", "Desc", 10);
        group.addPlan("pro", "Pro", "Pro plan", "Hot", 1);
        assertThat(group.getPlans()).hasSize(1);
        assertThat(group.getPlans().getFirst().getCode()).isEqualTo("pro");
    }

    @Test
    void addPlanWithPrice() {
        PlanGroup group = new PlanGroup("api_plans", "API Plans", "Desc", 10);
        Plan plan = group.addPlan("pro", "Pro", "Pro plan", "Hot", 1);
        plan.addPrice("CNY", "monthly", BigDecimal.valueOf(29), null);
        assertThat(plan.getPrices()).hasSize(1);
    }

    @Test
    void addPlanWithFeature() {
        PlanGroup group = new PlanGroup("api_plans", "API Plans", "Desc", 10);
        Plan plan = group.addPlan("pro", "Pro", "Pro plan", "Hot", 1);
        plan.addFeature("Storage", "100GB", true, 1);
        assertThat(plan.getFeatures()).hasSize(1);
    }

    @Test
    void disableGroup() {
        PlanGroup group = new PlanGroup("api_plans", "API Plans", "Desc", 10);
        group.disable();
        assertThat(group.isEnabled()).isFalse();
    }
}

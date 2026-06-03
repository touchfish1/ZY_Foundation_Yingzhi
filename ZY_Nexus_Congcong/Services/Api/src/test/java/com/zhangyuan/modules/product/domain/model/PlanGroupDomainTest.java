package com.zhangyuan.modules.product.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class PlanGroupDomainTest {

    @Test
    void create() {
        PlanGroup g = new PlanGroup("api_plans", "API Plans", "Desc", 10);
        assertThat(g.getCode()).isEqualTo("api_plans");
        assertThat(g.isEnabled()).isTrue();
    }

    @Test
    void addPlan() {
        PlanGroup g = new PlanGroup("api_plans", "API Plans", "Desc", 10);
        Plan plan = g.addPlan("pro", "Pro", "Pro plan", "Hot", 1);
        assertThat(g.getPlans()).hasSize(1);
        assertThat(plan.getCode()).isEqualTo("pro");
    }

    @Test
    void addPriceAndFeature() {
        PlanGroup g = new PlanGroup("api_plans", "API Plans", "Desc", 10);
        Plan plan = g.addPlan("pro", "Pro", "Pro plan", "Hot", 1);
        plan.addPrice("CNY", "monthly", BigDecimal.valueOf(29), null);
        plan.addFeature("Storage", "100GB", true, 1);
        assertThat(plan.getPrices()).hasSize(1);
        assertThat(plan.getFeatures()).hasSize(1);
    }

    @Test
    void disableAndEnable() {
        PlanGroup g = new PlanGroup("api_plans", "API Plans", "Desc", 10);
        g.disable();
        assertThat(g.isEnabled()).isFalse();
        g.enable();
        assertThat(g.isEnabled()).isTrue();
    }
}

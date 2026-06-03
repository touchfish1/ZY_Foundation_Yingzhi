package com.zhangyuan.modules.product.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Plan {

    private Long id;
    private PlanGroup group;
    private String code;
    private String name;
    private String description;
    private String badge;
    private boolean enabled;
    private int sortOrder;
    private List<Price> prices;
    private List<Feature> features;

    public Plan(PlanGroup group, String code, String name, String description, String badge, int sortOrder) {
        this.group = group;
        this.code = code;
        this.name = name;
        this.description = description;
        this.badge = badge;
        this.sortOrder = sortOrder;
        this.enabled = true;
        this.prices = new ArrayList<>();
        this.features = new ArrayList<>();
    }

    public void updateInfo(String name, String description, String badge, int sortOrder) {
        this.name = name;
        this.description = description;
        this.badge = badge;
        this.sortOrder = sortOrder;
    }

    public void disable() { this.enabled = false; }
    public void enable() { this.enabled = true; }

    public Price addPrice(String currency, String billingCycle, BigDecimal amount, BigDecimal originalAmount) {
        Price price = new Price(this, currency, billingCycle, amount, originalAmount);
        prices.add(price);
        return price;
    }

    public Feature addFeature(String featureName, String featureValue, boolean included, int sortOrder) {
        Feature feature = new Feature(this, featureName, featureValue, included, sortOrder);
        features.add(feature);
        return feature;
    }

    public Long getId() { return id; }
    public PlanGroup getGroup() { return group; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBadge() { return badge; }
    public boolean isEnabled() { return enabled; }
    public int getSortOrder() { return sortOrder; }
    public List<Price> getPrices() { return Collections.unmodifiableList(prices); }
    public List<Feature> getFeatures() { return Collections.unmodifiableList(features); }
}

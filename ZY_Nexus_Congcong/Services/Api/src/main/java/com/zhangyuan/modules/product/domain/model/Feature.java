package com.zhangyuan.modules.product.domain.model;

public class Feature {

    private Long id;
    private Plan plan;
    private String featureName;
    private String featureValue;
    private boolean included;
    private int sortOrder;

    public Feature(Plan plan, String featureName, String featureValue, boolean included, int sortOrder) {
        this.plan = plan;
        this.featureName = featureName;
        this.featureValue = featureValue;
        this.included = included;
        this.sortOrder = sortOrder;
    }

    public void update(String featureName, String featureValue, boolean included, int sortOrder) {
        this.featureName = featureName;
        this.featureValue = featureValue;
        this.included = included;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Plan getPlan() { return plan; }
    public String getFeatureName() { return featureName; }
    public String getFeatureValue() { return featureValue; }
    public boolean isIncluded() { return included; }
    public int getSortOrder() { return sortOrder; }
}

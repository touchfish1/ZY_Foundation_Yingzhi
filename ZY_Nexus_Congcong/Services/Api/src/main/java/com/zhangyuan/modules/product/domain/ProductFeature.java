package com.zhangyuan.modules.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_feature")
public class ProductFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "feature_name", nullable = false, length = 128)
    private String featureName;

    @Column(name = "feature_value", length = 255)
    private String featureValue;

    @Column(nullable = false)
    private Boolean included = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    protected ProductFeature() {
    }

    public ProductFeature(Long planId, String featureName, String featureValue, Boolean included, Integer sortOrder) {
        this.planId = planId;
        this.featureName = featureName;
        this.featureValue = featureValue;
        this.included = included == null || included;
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    public Long getId() {
        return id;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFeatureValue() {
        return featureValue;
    }

    public Boolean getIncluded() {
        return included;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}

package com.zhangyuan.modules.product.adapter.out.persistence;

import com.zhangyuan.modules.product.domain.model.Feature;
import com.zhangyuan.modules.product.domain.model.Plan;
import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.domain.model.Price;
import com.zhangyuan.modules.product.domain.repository.PlanGroupRepository;
import com.zhangyuan.modules.product.repository.ProductFeatureRepository;
import com.zhangyuan.modules.product.repository.ProductPlanGroupRepository;
import com.zhangyuan.modules.product.repository.ProductPlanRepository;
import com.zhangyuan.modules.product.repository.ProductPriceRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

@Component
public class JpaPlanGroupRepository implements PlanGroupRepository {

    private final ProductPlanGroupRepository jpaRepository;
    private final ProductPlanRepository planRepository;
    private final ProductPriceRepository priceRepository;
    private final ProductFeatureRepository featureRepository;

    public JpaPlanGroupRepository(ProductPlanGroupRepository jpaRepository,
                                  ProductPlanRepository planRepository,
                                  ProductPriceRepository priceRepository,
                                  ProductFeatureRepository featureRepository) {
        this.jpaRepository = jpaRepository;
        this.planRepository = planRepository;
        this.priceRepository = priceRepository;
        this.featureRepository = featureRepository;
    }

    @Override
    public Optional<PlanGroup> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<PlanGroup> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public List<PlanGroup> findAllOrdered() {
        return jpaRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PlanGroup save(PlanGroup group) {
        ProductPlanGroup entity;
        PlanGroup savedGroup;

        if (group.getId() != null) {
            // Existing group: just reload it
            entity = jpaRepository.findById(group.getId()).orElseThrow(
                    () -> new IllegalArgumentException("Product plan group not found: " + group.getId()));
            savedGroup = toDomain(entity);
        } else {
            // New group
            entity = new ProductPlanGroup(
                    group.getCode(), group.getName(), group.getDescription(), group.getSortOrder());
            entity = jpaRepository.save(entity);
            savedGroup = toDomain(entity);
        }

        // Save nested plans, prices, features
        for (Plan plan : group.getPlans()) {
            ProductPlan planEntity;
            Plan savedPlan;

            if (plan.getId() != null) {
                // Existing plan: reload it
                planEntity = planRepository.findById(plan.getId()).orElseThrow(
                        () -> new IllegalArgumentException("Product plan not found: " + plan.getId()));
                savedPlan = savedGroup.getPlans().stream()
                        .filter(p -> p.getId().equals(plan.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Saved plan not found in group"));
            } else {
                // New plan
                planEntity = new ProductPlan(
                        entity.getId(), plan.getCode(), plan.getName(), plan.getDescription(),
                        plan.getBadge(), plan.getSortOrder());
                planEntity = planRepository.save(planEntity);
                savedPlan = savedGroup.addPlan(plan.getCode(), plan.getName(), plan.getDescription(),
                        plan.getBadge(), plan.getSortOrder());
                setId(savedPlan, planEntity.getId());
            }

            // Save nested prices (only new ones)
            for (Price price : plan.getPrices()) {
                if (price.getId() != null) {
                    continue;
                }
                ProductPrice priceEntity = new ProductPrice(
                        planEntity.getId(), price.getCurrency(), price.getBillingCycle(),
                        price.getAmount(), price.getOriginalAmount());
                priceEntity = priceRepository.save(priceEntity);
                Price savedPrice = savedPlan.addPrice(price.getCurrency(), price.getBillingCycle(),
                        price.getAmount(), price.getOriginalAmount());
                setId(savedPrice, priceEntity.getId());
            }

            // Save nested features (only new ones)
            for (Feature feature : plan.getFeatures()) {
                if (feature.getId() != null) {
                    continue;
                }
                ProductFeature featureEntity = new ProductFeature(
                        planEntity.getId(), feature.getFeatureName(), feature.getFeatureValue(),
                        feature.isIncluded(), feature.getSortOrder());
                featureEntity = featureRepository.save(featureEntity);
                Feature savedFeature = savedPlan.addFeature(feature.getFeatureName(), feature.getFeatureValue(),
                        feature.isIncluded(), feature.getSortOrder());
                setId(savedFeature, featureEntity.getId());
            }
        }

        return findById(entity.getId()).orElse(savedGroup);
    }

    @Override
    public void deleteById(Long id) {
        // Delete nested entities first
        List<ProductPlan> plans = planRepository.findByGroupIdOrderBySortOrderAsc(id);
        for (ProductPlan plan : plans) {
            priceRepository.deleteByPlanId(plan.getId());
            featureRepository.deleteByPlanId(plan.getId());
        }
        planRepository.deleteByGroupId(id);
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.findByCode(code).isPresent();
    }

    private PlanGroup toDomain(ProductPlanGroup entity) {
        PlanGroup group = new PlanGroup(entity.getCode(), entity.getName(), entity.getDescription(), entity.getSortOrder());
        setId(group, entity.getId());

        // Load nested plans
        List<ProductPlan> planEntities = planRepository.findByGroupIdOrderBySortOrderAsc(entity.getId());
        for (ProductPlan planEntity : planEntities) {
            Plan plan = group.addPlan(planEntity.getCode(), planEntity.getName(), planEntity.getDescription(),
                    planEntity.getBadge(), planEntity.getSortOrder());
            setId(plan, planEntity.getId());

            // Load nested prices
            List<ProductPrice> priceEntities = priceRepository.findByPlanId(planEntity.getId());
            for (ProductPrice priceEntity : priceEntities) {
                Price price = plan.addPrice(priceEntity.getCurrency(), priceEntity.getBillingCycle(),
                        priceEntity.getAmount(), priceEntity.getOriginalAmount());
                setId(price, priceEntity.getId());
            }

            // Load nested features
            List<ProductFeature> featureEntities = featureRepository.findByPlanIdOrderBySortOrderAsc(planEntity.getId());
            for (ProductFeature featureEntity : featureEntities) {
                Feature feature = plan.addFeature(featureEntity.getFeatureName(), featureEntity.getFeatureValue(),
                        featureEntity.getIncluded(), featureEntity.getSortOrder());
                setId(feature, featureEntity.getId());
            }
        }

        return group;
    }

    private void setId(Object obj, Long id) {
        try {
            Field idField = obj.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id on " + obj.getClass().getName(), e);
        }
    }
}

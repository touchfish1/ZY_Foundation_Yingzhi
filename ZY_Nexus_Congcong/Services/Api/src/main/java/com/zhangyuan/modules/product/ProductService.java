package com.zhangyuan.modules.product;

import com.zhangyuan.modules.product.domain.ProductFeature;
import com.zhangyuan.modules.product.domain.ProductPlan;
import com.zhangyuan.modules.product.domain.ProductPlanGroup;
import com.zhangyuan.modules.product.domain.ProductPrice;
import com.zhangyuan.modules.product.dto.CreateFeatureRequest;
import com.zhangyuan.modules.product.dto.CreatePlanGroupRequest;
import com.zhangyuan.modules.product.dto.CreatePlanRequest;
import com.zhangyuan.modules.product.dto.CreatePriceRequest;
import com.zhangyuan.modules.product.dto.FeatureResponse;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import com.zhangyuan.modules.product.dto.PlanResponse;
import com.zhangyuan.modules.product.dto.PriceResponse;
import com.zhangyuan.modules.product.repository.ProductFeatureRepository;
import com.zhangyuan.modules.product.repository.ProductPlanGroupRepository;
import com.zhangyuan.modules.product.repository.ProductPlanRepository;
import com.zhangyuan.modules.product.repository.ProductPriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductPlanGroupRepository groupRepository;
    private final ProductPlanRepository planRepository;
    private final ProductPriceRepository priceRepository;
    private final ProductFeatureRepository featureRepository;

    public ProductService(ProductPlanGroupRepository groupRepository, ProductPlanRepository planRepository,
                          ProductPriceRepository priceRepository, ProductFeatureRepository featureRepository) {
        this.groupRepository = groupRepository;
        this.planRepository = planRepository;
        this.priceRepository = priceRepository;
        this.featureRepository = featureRepository;
    }

    @Transactional(readOnly = true)
    public List<PlanGroupResponse> listGroups() {
        return groupRepository.findAllByOrderBySortOrderAsc().stream().map(this::toGroupResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlanGroupResponse getGroupByCode(String code) {
        return groupRepository.findByCode(code)
                .map(this::toGroupResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product plan group not found"));
    }

    @Transactional
    public PlanGroupResponse createGroup(CreatePlanGroupRequest request) {
        if (groupRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Product plan group code already exists");
        }
        return toGroupResponse(groupRepository.save(new ProductPlanGroup(request.code(), request.name(), request.description(), request.sortOrder())));
    }

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        if (!groupRepository.existsById(request.groupId())) {
            throw new IllegalArgumentException("Product plan group not found");
        }
        if (planRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Product plan code already exists");
        }
        ProductPlan plan = planRepository.save(new ProductPlan(request.groupId(), request.code(), request.name(), request.description(), request.badge(), request.sortOrder()));
        return toPlanResponse(plan);
    }

    @Transactional
    public PriceResponse createPrice(CreatePriceRequest request) {
        if (!planRepository.existsById(request.planId())) {
            throw new IllegalArgumentException("Product plan not found");
        }
        ProductPrice price = priceRepository.save(new ProductPrice(request.planId(), request.currency(), request.billingCycle(), request.amount(), request.originalAmount()));
        return toPriceResponse(price);
    }

    @Transactional
    public FeatureResponse createFeature(CreateFeatureRequest request) {
        if (!planRepository.existsById(request.planId())) {
            throw new IllegalArgumentException("Product plan not found");
        }
        ProductFeature feature = featureRepository.save(new ProductFeature(request.planId(), request.featureName(), request.featureValue(), request.included(), request.sortOrder()));
        return toFeatureResponse(feature);
    }

    private PlanGroupResponse toGroupResponse(ProductPlanGroup group) {
        List<PlanResponse> plans = planRepository.findByGroupIdOrderBySortOrderAsc(group.getId()).stream()
                .map(this::toPlanResponse)
                .toList();
        return new PlanGroupResponse(group.getId(), group.getCode(), group.getName(), group.getDescription(), group.getStatus(), group.getSortOrder(), plans);
    }

    private PlanResponse toPlanResponse(ProductPlan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getBadge(),
                plan.getStatus(),
                plan.getSortOrder(),
                priceRepository.findByPlanId(plan.getId()).stream().map(this::toPriceResponse).toList(),
                featureRepository.findByPlanIdOrderBySortOrderAsc(plan.getId()).stream().map(this::toFeatureResponse).toList()
        );
    }

    private PriceResponse toPriceResponse(ProductPrice price) {
        return new PriceResponse(price.getId(), price.getCurrency(), price.getBillingCycle(), price.getAmount(), price.getOriginalAmount(), price.getStatus());
    }

    private FeatureResponse toFeatureResponse(ProductFeature feature) {
        return new FeatureResponse(feature.getId(), feature.getFeatureName(), feature.getFeatureValue(), feature.getIncluded(), feature.getSortOrder());
    }
}

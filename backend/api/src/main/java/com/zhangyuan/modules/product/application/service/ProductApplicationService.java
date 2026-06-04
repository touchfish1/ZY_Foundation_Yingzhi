package com.zhangyuan.modules.product.application.service;

import com.zhangyuan.modules.product.adapter.out.persistence.ProductFeature;
import com.zhangyuan.modules.product.adapter.out.persistence.ProductPlan;
import com.zhangyuan.modules.product.adapter.out.persistence.ProductPlanGroup;
import com.zhangyuan.modules.product.adapter.out.persistence.ProductPrice;
import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.domain.repository.PlanGroupRepository;
import com.zhangyuan.modules.product.domain.service.ProductDomainService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ProductApplicationService.class);

    private final PlanGroupRepository planGroupRepository;
    private final ProductDomainService productDomainService;
    private final ProductPlanGroupRepository groupJpaRepository;
    private final ProductPlanRepository planJpaRepository;
    private final ProductPriceRepository priceJpaRepository;
    private final ProductFeatureRepository featureJpaRepository;

    public ProductApplicationService(PlanGroupRepository planGroupRepository,
                                     ProductDomainService productDomainService,
                                     ProductPlanGroupRepository groupJpaRepository,
                                     ProductPlanRepository planJpaRepository,
                                     ProductPriceRepository priceJpaRepository,
                                     ProductFeatureRepository featureJpaRepository) {
        this.planGroupRepository = planGroupRepository;
        this.productDomainService = productDomainService;
        this.groupJpaRepository = groupJpaRepository;
        this.planJpaRepository = planJpaRepository;
        this.priceJpaRepository = priceJpaRepository;
        this.featureJpaRepository = featureJpaRepository;
    }

    // ---- PlanGroup (domain-based) ----

    @Transactional
    public PlanGroup createGroup(String code, String name, String description, int sortOrder) {
        log.info("Creating plan group: code={}, name={}", code, name);
        productDomainService.validateGroupCreation(planGroupRepository, code);
        PlanGroup group = new PlanGroup(code, name, description, sortOrder);
        PlanGroup saved = planGroupRepository.save(group);
        log.info("Plan group created: id={}, code={}", saved.getId(), saved.getCode());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<PlanGroup> findByCode(String code) {
        return planGroupRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public List<PlanGroup> listAll() {
        return planGroupRepository.findAllOrdered();
    }

    @Transactional
    public void disableGroup(Long id) {
        log.info("Disabling plan group: {}", id);
        PlanGroup group = planGroupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Plan group not found: {}", id);
                    return new IllegalArgumentException("Plan group not found: " + id);
                });
        group.disable();
        planGroupRepository.save(group);
        log.info("Plan group disabled: {}", id);
    }

    // ---- DTO-based operations (migrated from ProductService) ----

    @Transactional(readOnly = true)
    public List<PlanGroupResponse> listGroups() {
        return groupJpaRepository.findAllByOrderBySortOrderAsc().stream().map(this::toGroupResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlanGroupResponse getGroupByCode(String code) {
        return groupJpaRepository.findByCode(code)
                .map(this::toGroupResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product plan group not found"));
    }

    @Transactional(readOnly = true)
    public Optional<PlanGroupResponse> findGroupByCode(String code) {
        return groupJpaRepository.findByCode(code).map(this::toGroupResponse);
    }

    @Transactional
    public PlanGroupResponse createGroup(CreatePlanGroupRequest request) {
        if (groupJpaRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Product plan group code already exists");
        }
        return toGroupResponse(groupJpaRepository.save(new ProductPlanGroup(request.code(), request.name(), request.description(), request.sortOrder())));
    }

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        if (!groupJpaRepository.existsById(request.groupId())) {
            throw new IllegalArgumentException("Product plan group not found");
        }
        if (planJpaRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Product plan code already exists");
        }
        ProductPlan plan = planJpaRepository.save(new ProductPlan(request.groupId(), request.code(), request.name(), request.description(), request.badge(), request.sortOrder()));
        return toPlanResponse(plan);
    }

    @Transactional
    public PriceResponse createPrice(CreatePriceRequest request) {
        if (!planJpaRepository.existsById(request.planId())) {
            throw new IllegalArgumentException("Product plan not found");
        }
        ProductPrice price = priceJpaRepository.save(new ProductPrice(request.planId(), request.currency(), request.billingCycle(), request.amount(), request.originalAmount()));
        return toPriceResponse(price);
    }

    @Transactional
    public FeatureResponse createFeature(CreateFeatureRequest request) {
        if (!planJpaRepository.existsById(request.planId())) {
            throw new IllegalArgumentException("Product plan not found");
        }
        ProductFeature feature = featureJpaRepository.save(new ProductFeature(request.planId(), request.featureName(), request.featureValue(), request.included(), request.sortOrder()));
        return toFeatureResponse(feature);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listPlans() {
        return planJpaRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toPlanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PriceResponse> listPrices() {
        return priceJpaRepository.findAllByOrderByPlanIdAsc().stream()
                .map(this::toPriceResponse)
                .toList();
    }

    // ---- mapping helpers ----

    private PlanGroupResponse toGroupResponse(ProductPlanGroup group) {
        List<PlanResponse> plans = planJpaRepository.findByGroupIdOrderBySortOrderAsc(group.getId()).stream()
                .map(this::toPlanResponse)
                .toList();
        return new PlanGroupResponse(group.getId(), group.getCode(), group.getName(), group.getDescription(), group.getStatus(), group.getSortOrder(), plans);
    }

    private PlanResponse toPlanResponse(ProductPlan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getGroupId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getBadge(),
                plan.getStatus(),
                plan.getSortOrder(),
                priceJpaRepository.findByPlanId(plan.getId()).stream().map(this::toPriceResponse).toList(),
                featureJpaRepository.findByPlanIdOrderBySortOrderAsc(plan.getId()).stream().map(this::toFeatureResponse).toList()
        );
    }

    private PriceResponse toPriceResponse(ProductPrice price) {
        return new PriceResponse(price.getId(), price.getCurrency(), price.getBillingCycle(), price.getAmount(), price.getOriginalAmount(), price.getStatus());
    }

    private FeatureResponse toFeatureResponse(ProductFeature feature) {
        return new FeatureResponse(feature.getId(), feature.getFeatureName(), feature.getFeatureValue(), feature.getIncluded(), feature.getSortOrder());
    }
}

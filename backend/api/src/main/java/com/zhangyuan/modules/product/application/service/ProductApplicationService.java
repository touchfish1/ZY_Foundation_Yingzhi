package com.zhangyuan.modules.product.application.service;

import com.zhangyuan.modules.product.domain.model.Feature;
import com.zhangyuan.modules.product.domain.model.Plan;
import com.zhangyuan.modules.product.domain.model.PlanGroup;
import com.zhangyuan.modules.product.domain.model.Price;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProductApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ProductApplicationService.class);

    private final PlanGroupRepository planGroupRepository;
    private final ProductDomainService productDomainService;

    public ProductApplicationService(PlanGroupRepository planGroupRepository,
                                     ProductDomainService productDomainService) {
        this.planGroupRepository = planGroupRepository;
        this.productDomainService = productDomainService;
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
        return planGroupRepository.findAllOrdered().stream().map(this::toGroupResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlanGroupResponse getGroupByCode(String code) {
        return planGroupRepository.findByCode(code)
                .map(this::toGroupResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product plan group not found"));
    }

    @Transactional(readOnly = true)
    public Optional<PlanGroupResponse> findGroupByCode(String code) {
        return planGroupRepository.findByCode(code).map(this::toGroupResponse);
    }

    @Transactional
    public PlanGroupResponse createGroup(CreatePlanGroupRequest request) {
        if (planGroupRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Product plan group code already exists");
        }
        PlanGroup group = new PlanGroup(request.code(), request.name(), request.description(),
                request.sortOrder() != null ? request.sortOrder() : 0);
        return toGroupResponse(planGroupRepository.save(group));
    }

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        PlanGroup group = planGroupRepository.findById(request.groupId())
                .orElseThrow(() -> new IllegalArgumentException("Product plan group not found"));
        boolean codeExists = planGroupRepository.findAllOrdered().stream()
                .flatMap(pg -> pg.getPlans().stream())
                .anyMatch(p -> p.getCode().equals(request.code()));
        if (codeExists) {
            throw new IllegalArgumentException("Product plan code already exists");
        }
        Plan plan = group.addPlan(request.code(), request.name(), request.description(), request.badge(),
                request.sortOrder() != null ? request.sortOrder() : 0);
        planGroupRepository.save(group);
        return toPlanResponse(plan);
    }

    @Transactional
    public PriceResponse createPrice(CreatePriceRequest request) {
        PlanGroup group = planGroupRepository.findAllOrdered().stream()
                .filter(pg -> pg.getPlans().stream().anyMatch(p -> p.getId().equals(request.planId())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product plan not found"));
        Plan plan = group.getPlans().stream()
                .filter(p -> p.getId().equals(request.planId()))
                .findFirst().get();
        Price price = plan.addPrice(request.currency(), request.billingCycle(), request.amount(), request.originalAmount());
        planGroupRepository.save(group);
        return toPriceResponse(price);
    }

    @Transactional
    public FeatureResponse createFeature(CreateFeatureRequest request) {
        PlanGroup group = planGroupRepository.findAllOrdered().stream()
                .filter(pg -> pg.getPlans().stream().anyMatch(p -> p.getId().equals(request.planId())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product plan not found"));
        Plan plan = group.getPlans().stream()
                .filter(p -> p.getId().equals(request.planId()))
                .findFirst().get();
        Feature feature = plan.addFeature(request.featureName(), request.featureValue(),
                request.included() == null || request.included(), request.sortOrder() != null ? request.sortOrder() : 0);
        planGroupRepository.save(group);
        return toFeatureResponse(feature);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listPlans() {
        return planGroupRepository.findAllOrdered().stream()
                .flatMap(pg -> pg.getPlans().stream())
                .sorted(Comparator.comparingInt(Plan::getSortOrder))
                .map(this::toPlanResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PriceResponse> listPrices() {
        return planGroupRepository.findAllOrdered().stream()
                .flatMap(pg -> pg.getPlans().stream())
                .flatMap(p -> p.getPrices().stream())
                .sorted(Comparator.comparingLong(p -> p.getPlan().getId()))
                .map(this::toPriceResponse)
                .toList();
    }

    // ---- mapping helpers ----

    private PlanGroupResponse toGroupResponse(PlanGroup group) {
        List<PlanResponse> plans = group.getPlans().stream()
                .map(this::toPlanResponse)
                .toList();
        return new PlanGroupResponse(group.getId(), group.getCode(), group.getName(), group.getDescription(),
                group.isEnabled() ? "enabled" : "disabled", group.getSortOrder(), plans);
    }

    private PlanResponse toPlanResponse(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getGroup().getId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getBadge(),
                plan.isEnabled() ? "enabled" : "disabled",
                plan.getSortOrder(),
                plan.getPrices().stream().map(this::toPriceResponse).toList(),
                plan.getFeatures().stream().map(this::toFeatureResponse).toList()
        );
    }

    private PriceResponse toPriceResponse(Price price) {
        return new PriceResponse(price.getId(), price.getCurrency(), price.getBillingCycle(), price.getAmount(), price.getOriginalAmount(),
                price.isEnabled() ? "enabled" : "disabled");
    }

    private FeatureResponse toFeatureResponse(Feature feature) {
        return new FeatureResponse(feature.getId(), feature.getFeatureName(), feature.getFeatureValue(), feature.isIncluded(), feature.getSortOrder());
    }
}

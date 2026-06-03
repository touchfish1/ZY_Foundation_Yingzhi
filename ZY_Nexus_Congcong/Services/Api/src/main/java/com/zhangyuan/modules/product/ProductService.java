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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 产品服务，提供产品方案组、方案、价格和特性的增删改查功能。
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

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

    /**
     * 获取所有方案组列表。
     *
     * @return 方案组响应列表
     */
    @Transactional(readOnly = true)
    public List<PlanGroupResponse> listGroups() {
        return groupRepository.findAllByOrderBySortOrderAsc().stream().map(this::toGroupResponse).toList();
    }

    /**
     * 根据编码查询方案组。
     *
     * @param code 方案组编码
     * @return 方案组响应
     */
    @Transactional(readOnly = true)
    public PlanGroupResponse getGroupByCode(String code) {
        return groupRepository.findByCode(code)
                .map(this::toGroupResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product plan group not found"));
    }

    /**
     * 根据编码查询方案组（返回 Optional）。
     *
     * @param code 方案组编码
     * @return 方案组响应 Optional
     */
    @Transactional(readOnly = true)
    public Optional<PlanGroupResponse> findGroupByCode(String code) {
        return groupRepository.findByCode(code).map(this::toGroupResponse);
    }

    /**
     * 创建方案组。
     *
     * @param request 创建方案组请求
     * @return 创建后的方案组
     */
    @Transactional
    public PlanGroupResponse createGroup(CreatePlanGroupRequest request) {
        log.info("Creating plan group: code={}, name={}", request.code(), request.name());
        // 检查编码唯一性
        if (groupRepository.existsByCode(request.code())) {
            log.warn("Product plan group code already exists: {}", request.code());
            throw new IllegalArgumentException("Product plan group code already exists");
        }
        return toGroupResponse(groupRepository.save(new ProductPlanGroup(request.code(), request.name(), request.description(), request.sortOrder())));
    }

    /**
     * 创建产品方案。
     *
     * @param request 创建方案请求
     * @return 创建后的方案
     */
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        log.info("Creating plan: code={}, groupId={}", request.code(), request.groupId());
        // 验证方案组存在
        if (!groupRepository.existsById(request.groupId())) {
            log.warn("Product plan group not found: {}", request.groupId());
            throw new IllegalArgumentException("Product plan group not found");
        }
        // 检查方案编码唯一性
        if (planRepository.existsByCode(request.code())) {
            log.warn("Product plan code already exists: {}", request.code());
            throw new IllegalArgumentException("Product plan code already exists");
        }
        ProductPlan plan = planRepository.save(new ProductPlan(request.groupId(), request.code(), request.name(), request.description(), request.badge(), request.sortOrder()));
        log.info("Plan created: id={}, code={}", plan.getId(), plan.getCode());
        return toPlanResponse(plan);
    }

    /**
     * 创建价格配置。
     *
     * @param request 创建价格请求
     * @return 创建后的价格
     */
    @Transactional
    public PriceResponse createPrice(CreatePriceRequest request) {
        log.info("Creating price for plan: {}", request.planId());
        if (!planRepository.existsById(request.planId())) {
            log.warn("Product plan not found: {}", request.planId());
            throw new IllegalArgumentException("Product plan not found");
        }
        ProductPrice price = priceRepository.save(new ProductPrice(request.planId(), request.currency(), request.billingCycle(), request.amount(), request.originalAmount()));
        log.info("Price created: id={}", price.getId());
        return toPriceResponse(price);
    }

    /**
     * 创建产品特性。
     *
     * @param request 创建特性请求
     * @return 创建后的特性
     */
    @Transactional
    public FeatureResponse createFeature(CreateFeatureRequest request) {
        log.info("Creating feature for plan: {}", request.planId());
        if (!planRepository.existsById(request.planId())) {
            log.warn("Product plan not found: {}", request.planId());
            throw new IllegalArgumentException("Product plan not found");
        }
        ProductFeature feature = featureRepository.save(new ProductFeature(request.planId(), request.featureName(), request.featureValue(), request.included(), request.sortOrder()));
        log.info("Feature created: id={}", feature.getId());
        return toFeatureResponse(feature);
    }

    /**
     * 将方案组实体转换为响应对象（含关联方案）。
     */
    private PlanGroupResponse toGroupResponse(ProductPlanGroup group) {
        List<PlanResponse> plans = planRepository.findByGroupIdOrderBySortOrderAsc(group.getId()).stream()
                .map(this::toPlanResponse)
                .toList();
        return new PlanGroupResponse(group.getId(), group.getCode(), group.getName(), group.getDescription(), group.getStatus(), group.getSortOrder(), plans);
    }

    /**
     * 获取所有方案列表。
     *
     * @return 方案响应列表
     */
    @Transactional(readOnly = true)
    public List<PlanResponse> listPlans() {
        return planRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toPlanResponse)
                .toList();
    }

    /**
     * 获取所有价格列表。
     *
     * @return 价格响应列表
     */
    @Transactional(readOnly = true)
    public List<PriceResponse> listPrices() {
        return priceRepository.findAllByOrderByPlanIdAsc().stream()
                .map(this::toPriceResponse)
                .toList();
    }

    /**
     * 将方案实体转换为响应对象（含关联价格和特性）。
     */
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

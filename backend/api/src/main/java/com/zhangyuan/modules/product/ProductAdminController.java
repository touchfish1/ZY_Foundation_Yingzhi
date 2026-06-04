package com.zhangyuan.modules.product;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.dto.CreateFeatureRequest;
import com.zhangyuan.modules.product.dto.CreatePlanGroupRequest;
import com.zhangyuan.modules.product.dto.CreatePlanRequest;
import com.zhangyuan.modules.product.dto.CreatePriceRequest;
import com.zhangyuan.modules.product.dto.FeatureResponse;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import com.zhangyuan.modules.product.dto.PlanResponse;
import com.zhangyuan.modules.product.dto.PriceResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台产品管理控制器，提供产品方案组、方案、价格和特性的增删改查接口。
 */
@RestController
@RequestMapping("/admin/product")
public class ProductAdminController {

    private static final Logger log = LoggerFactory.getLogger(ProductAdminController.class);

    private final ProductService productService;

    public ProductAdminController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取所有产品方案组列表。
     *
     * @return 方案组响应列表
     */
    @GetMapping("/plan-groups")
    public ApiResponse<List<PlanGroupResponse>> listGroups() {
        log.info("Listing product plan groups");
        return ApiResponse.ok(productService.listGroups());
    }

    /**
     * 获取所有产品方案列表。
     *
     * @return 方案响应列表
     */
    @GetMapping("/plans")
    public ApiResponse<List<PlanResponse>> listPlans() {
        log.info("Listing product plans");
        return ApiResponse.ok(productService.listPlans());
    }

    /**
     * 获取所有价格列表。
     *
     * @return 价格响应列表
     */
    @GetMapping("/prices")
    public ApiResponse<List<PriceResponse>> listPrices() {
        log.info("Listing product prices");
        return ApiResponse.ok(productService.listPrices());
    }

    /**
     * 创建产品方案组。
     *
     * @param request 创建方案组请求
     * @return 创建后的方案组
     */
    @PostMapping("/plan-groups")
    public ApiResponse<PlanGroupResponse> createGroup(@Valid @RequestBody CreatePlanGroupRequest request) {
        log.info("Creating plan group: code={}, name={}", request.code(), request.name());
        return ApiResponse.ok(productService.createGroup(request));
    }

    /**
     * 创建产品方案。
     *
     * @param request 创建方案请求
     * @return 创建后的方案
     */
    @PostMapping("/plans")
    public ApiResponse<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        log.info("Creating plan: code={}, groupId={}", request.code(), request.groupId());
        return ApiResponse.ok(productService.createPlan(request));
    }

    /**
     * 创建价格配置。
     *
     * @param request 创建价格请求
     * @return 创建后的价格
     */
    @PostMapping("/prices")
    public ApiResponse<PriceResponse> createPrice(@Valid @RequestBody CreatePriceRequest request) {
        log.info("Creating price for plan: {}", request.planId());
        return ApiResponse.ok(productService.createPrice(request));
    }

    /**
     * 创建产品特性。
     *
     * @param request 创建特性请求
     * @return 创建后的特性
     */
    @PostMapping("/features")
    public ApiResponse<FeatureResponse> createFeature(@Valid @RequestBody CreateFeatureRequest request) {
        log.info("Creating feature for plan: {}", request.planId());
        return ApiResponse.ok(productService.createFeature(request));
    }
}

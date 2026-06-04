package com.zhangyuan.modules.product.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
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

@RestController
@RequestMapping("/admin/product")
@SaCheckPermission("product:manage")
public class AdminProductController {

    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    private final ProductApplicationService productApplicationService;

    public AdminProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @GetMapping("/plan-groups")
    public ApiResponse<List<PlanGroupResponse>> listGroups() {
        log.info("Listing product plan groups");
        return ApiResponse.ok(productApplicationService.listGroups());
    }

    @GetMapping("/plans")
    public ApiResponse<List<PlanResponse>> listPlans() {
        log.info("Listing product plans");
        return ApiResponse.ok(productApplicationService.listPlans());
    }

    @GetMapping("/prices")
    public ApiResponse<List<PriceResponse>> listPrices() {
        log.info("Listing product prices");
        return ApiResponse.ok(productApplicationService.listPrices());
    }

    @PostMapping("/plan-groups")
    public ApiResponse<PlanGroupResponse> createGroup(@Valid @RequestBody CreatePlanGroupRequest request) {
        log.info("Creating plan group: code={}, name={}", request.code(), request.name());
        return ApiResponse.ok(productApplicationService.createGroup(request));
    }

    @PostMapping("/plans")
    public ApiResponse<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        log.info("Creating plan: code={}, groupId={}", request.code(), request.groupId());
        return ApiResponse.ok(productApplicationService.createPlan(request));
    }

    @PostMapping("/prices")
    public ApiResponse<PriceResponse> createPrice(@Valid @RequestBody CreatePriceRequest request) {
        log.info("Creating price for plan: {}", request.planId());
        return ApiResponse.ok(productApplicationService.createPrice(request));
    }

    @PostMapping("/features")
    public ApiResponse<FeatureResponse> createFeature(@Valid @RequestBody CreateFeatureRequest request) {
        log.info("Creating feature for plan: {}", request.planId());
        return ApiResponse.ok(productApplicationService.createFeature(request));
    }
}

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class ProductAdminController {

    private final ProductService productService;

    public ProductAdminController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/plan-groups")
    public ApiResponse<List<PlanGroupResponse>> listGroups() {
        return ApiResponse.ok(productService.listGroups());
    }

    @PostMapping("/plan-groups")
    public ApiResponse<PlanGroupResponse> createGroup(@Valid @RequestBody CreatePlanGroupRequest request) {
        return ApiResponse.ok(productService.createGroup(request));
    }

    @PostMapping("/plans")
    public ApiResponse<PlanResponse> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        return ApiResponse.ok(productService.createPlan(request));
    }

    @PostMapping("/prices")
    public ApiResponse<PriceResponse> createPrice(@Valid @RequestBody CreatePriceRequest request) {
        return ApiResponse.ok(productService.createPrice(request));
    }

    @PostMapping("/features")
    public ApiResponse<FeatureResponse> createFeature(@Valid @RequestBody CreateFeatureRequest request) {
        return ApiResponse.ok(productService.createFeature(request));
    }
}

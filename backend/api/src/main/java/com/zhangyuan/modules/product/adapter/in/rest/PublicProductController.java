package com.zhangyuan.modules.product.adapter.in.rest;

import com.zhangyuan.common.operationlog.annotation.OperationLog;
import static com.zhangyuan.common.operationlog.domain.model.OperationType.*;
import static com.zhangyuan.common.operationlog.domain.model.ResourceType.*;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import com.zhangyuan.modules.product.dto.PlanResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class PublicProductController {

    private static final Logger log = LoggerFactory.getLogger(PublicProductController.class);

    private final ProductApplicationService productApplicationService;

    public PublicProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @GetMapping("/plan-groups/{code}")
    @OperationLog(type = QUERY, resource = PRODUCT_PLAN_GROUP, resourceId = "#code")
    public ApiResponse<PlanGroupResponse> getGroup(@PathVariable String code) {
        log.info("Getting product plan group by code: {}", code);
        return ApiResponse.ok(productApplicationService.getGroupByCode(code));
    }

    @GetMapping("/plans")
    @Cacheable(value = "product:plans", unless = "#result == null || #result.data() == null")
    @OperationLog(type = QUERY, resource = PRODUCT_PLAN)
    public ApiResponse<List<PlanResponse>> listAvailablePlans() {
        log.info("Listing all available plans");
        return ApiResponse.ok(productApplicationService.listPlans());
    }
}

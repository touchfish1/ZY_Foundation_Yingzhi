package com.zhangyuan.modules.product.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class PublicProductController {

    private static final Logger log = LoggerFactory.getLogger(PublicProductController.class);

    private final ProductApplicationService productApplicationService;

    public PublicProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @GetMapping("/plan-groups/{code}")
    public ApiResponse<PlanGroupResponse> getGroup(@PathVariable String code) {
        log.info("Getting product plan group by code: {}", code);
        return ApiResponse.ok(productApplicationService.getGroupByCode(code));
    }
}

package com.zhangyuan.modules.product.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
import com.zhangyuan.modules.product.domain.model.PlanGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DDD 版产品控制器，提供产品方案组的查询和创建接口。
 */
@RestController
@RequestMapping("/api/ddd/product")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductApplicationService productApplicationService;

    public ProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    /**
     * 获取所有方案组列表。
     *
     * @return 方案组列表
     */
    @GetMapping("/plan-groups")
    public ApiResponse<List<PlanGroup>> listGroups() {
        log.info("Listing plan groups");
        return ApiResponse.ok(productApplicationService.listAll());
    }

    /**
     * 根据编码查询方案组。
     *
     * @param code 方案组编码
     * @return 方案组
     */
    @GetMapping("/plan-groups/{code}")
    public ApiResponse<PlanGroup> getGroup(@PathVariable String code) {
        log.info("Getting plan group by code: {}", code);
        return productApplicationService.findByCode(code)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "Plan group not found"));
    }

    /**
     * 创建新方案组。
     *
     * @param request 创建方案组请求
     * @return 创建后的方案组
     */
    @PostMapping("/plan-groups")
    public ApiResponse<PlanGroup> createGroup(@RequestBody CreateGroupRequest request) {
        log.info("Creating plan group: code={}, name={}", request.code(), request.name());
        PlanGroup group = productApplicationService.createGroup(
                request.code(), request.name(), request.description(), request.sortOrder());
        log.info("Plan group created: id={}, code={}", group.getId(), group.getCode());
        return ApiResponse.ok(group);
    }

    public record CreateGroupRequest(String code, String name, String description, int sortOrder) {}
}

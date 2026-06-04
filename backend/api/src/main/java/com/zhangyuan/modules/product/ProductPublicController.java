package com.zhangyuan.modules.product;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开产品控制器，提供前台产品方案查询接口。
 */
@RestController
@RequestMapping("/api/products")
public class ProductPublicController {

    private static final Logger log = LoggerFactory.getLogger(ProductPublicController.class);

    private final ProductService productService;

    public ProductPublicController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 根据编码查询产品方案组。
     *
     * @param code 方案组编码
     * @return 方案组响应
     */
    @GetMapping("/plan-groups/{code}")
    public ApiResponse<PlanGroupResponse> getGroup(@PathVariable String code) {
        log.info("Getting product plan group by code: {}", code);
        return ApiResponse.ok(productService.getGroupByCode(code));
    }
}

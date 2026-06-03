package com.zhangyuan.modules.product;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductPublicController {

    private final ProductService productService;

    public ProductPublicController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/plan-groups/{code}")
    public ApiResponse<PlanGroupResponse> getGroup(@PathVariable String code) {
        return ApiResponse.ok(productService.getGroupByCode(code));
    }
}

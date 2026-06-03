package com.zhangyuan.modules.cms;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台 CMS 区块定义控制器，提供已启用区块定义的查询接口。
 */
@RestController
@RequestMapping("/admin/cms/block-definitions")
public class CmsBlockDefinitionController {

    private static final Logger log = LoggerFactory.getLogger(CmsBlockDefinitionController.class);

    private final CmsBlockDefinitionService service;

    public CmsBlockDefinitionController(CmsBlockDefinitionService service) {
        this.service = service;
    }

    /**
     * 获取所有已启用的区块定义列表。
     *
     * @return 区块定义响应列表
     */
    @GetMapping
    public ApiResponse<List<BlockDefinitionResponse>> listEnabled() {
        log.info("Listing enabled block definitions");
        return ApiResponse.ok(service.listEnabled());
    }
}

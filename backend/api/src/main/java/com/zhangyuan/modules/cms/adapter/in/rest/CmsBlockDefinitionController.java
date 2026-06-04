package com.zhangyuan.modules.cms.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.application.service.CmsApplicationService;
import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/cms/block-definitions")
@SaCheckPermission("cms:blocks")
public class CmsBlockDefinitionController {

    private static final Logger log = LoggerFactory.getLogger(CmsBlockDefinitionController.class);

    private final CmsApplicationService cmsApplicationService;

    public CmsBlockDefinitionController(CmsApplicationService cmsApplicationService) {
        this.cmsApplicationService = cmsApplicationService;
    }

    @GetMapping
    public ApiResponse<List<BlockDefinitionResponse>> listEnabled() {
        log.info("Listing enabled block definitions");
        return ApiResponse.ok(cmsApplicationService.listEnabledBlockDefinitions());
    }
}

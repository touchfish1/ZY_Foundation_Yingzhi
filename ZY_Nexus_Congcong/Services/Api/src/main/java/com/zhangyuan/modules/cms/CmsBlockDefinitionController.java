package com.zhangyuan.modules.cms;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/cms/block-definitions")
public class CmsBlockDefinitionController {

    private final CmsBlockDefinitionService service;

    public CmsBlockDefinitionController(CmsBlockDefinitionService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<BlockDefinitionResponse>> listEnabled() {
        return ApiResponse.ok(service.listEnabled());
    }
}

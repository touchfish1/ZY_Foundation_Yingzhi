package com.zhangyuan.modules.cms.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.operationlog.annotation.OperationLog;
import static com.zhangyuan.common.operationlog.domain.model.OperationType.*;
import static com.zhangyuan.common.operationlog.domain.model.ResourceType.*;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.application.service.CmsApplicationService;
import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import com.zhangyuan.modules.cms.dto.CreateBlockDefinitionRequest;
import com.zhangyuan.modules.cms.dto.UpdateBlockDefinitionRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/cms/block-definitions")
@SaCheckPermission("cms:manage")
public class CmsBlockDefinitionController {

    private static final Logger log = LoggerFactory.getLogger(CmsBlockDefinitionController.class);

    private final CmsApplicationService cmsApplicationService;

    public CmsBlockDefinitionController(CmsApplicationService cmsApplicationService) {
        this.cmsApplicationService = cmsApplicationService;
    }

    @GetMapping
    @OperationLog(type = QUERY, resource = CMS_PAGE)
    public ApiResponse<List<BlockDefinitionResponse>> listEnabled() {
        log.info("Listing enabled block definitions");
        return ApiResponse.ok(cmsApplicationService.listEnabledBlockDefinitions());
    }

    @PostMapping
    @OperationLog(type = CREATE, resource = CMS_PAGE)
    public ApiResponse<BlockDefinitionResponse> create(@Valid @RequestBody CreateBlockDefinitionRequest request) {
        log.info("Creating block definition: type={}", request.type());
        return ApiResponse.ok(cmsApplicationService.createBlockDefinition(request));
    }

    @PutMapping("/{id}")
    @OperationLog(type = UPDATE, resource = CMS_PAGE)
    public ApiResponse<BlockDefinitionResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateBlockDefinitionRequest request) {
        log.info("Updating block definition: id={}", id);
        return ApiResponse.ok(cmsApplicationService.updateBlockDefinition(id, request));
    }

    @DeleteMapping("/{id}")
    @OperationLog(type = DELETE, resource = CMS_PAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("Deleting block definition: id={}", id);
        cmsApplicationService.deleteBlockDefinition(id);
        return ApiResponse.ok();
    }
}

package com.zhangyuan.modules.cms.adapter.in.rest;

import com.zhangyuan.common.operationlog.annotation.OperationLog;
import static com.zhangyuan.common.operationlog.domain.model.OperationType.*;
import static com.zhangyuan.common.operationlog.domain.model.ResourceType.*;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.cms.application.service.CmsApplicationService;
import com.zhangyuan.modules.cms.dto.PageListItemResponse;
import com.zhangyuan.modules.cms.dto.RenderPageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cms/pages")
public class CmsPublicController {

    private static final Logger log = LoggerFactory.getLogger(CmsPublicController.class);

    private final CmsApplicationService cmsApplicationService;

    public CmsPublicController(CmsApplicationService cmsApplicationService) {
        this.cmsApplicationService = cmsApplicationService;
    }

    @GetMapping("/render")
    @Cacheable(value = "cms:pages", key = "#path + ':' + (#locale ?: 'zh-CN')", unless = "#result == null || #result.data() == null")
    @OperationLog(type = QUERY, resource = CMS_PAGE)
    public ApiResponse<RenderPageResponse> render(@RequestParam("path") String path,
                                                  @RequestParam(value = "locale", required = false) String locale) {
        log.info("Rendering CMS page: path={}, locale={}", path, locale);
        return ApiResponse.ok(cmsApplicationService.render(path, locale));
    }

    @GetMapping("/list")
    @OperationLog(type = QUERY, resource = CMS_PAGE)
    public ApiResponse<PageResponse<PageListItemResponse>> listPublishedPages(
            @RequestParam(defaultValue = "custom") String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("Listing published CMS pages: type={}, page={}, pageSize={}", type, page, pageSize);
        return ApiResponse.ok(cmsApplicationService.listPublishedByType(type, page, pageSize));
    }
}

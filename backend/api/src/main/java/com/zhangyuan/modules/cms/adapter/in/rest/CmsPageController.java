package com.zhangyuan.modules.cms.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.application.service.CmsApplicationService;
import com.zhangyuan.modules.cms.dto.CreatePageRequest;
import com.zhangyuan.modules.cms.dto.PageDetailResponse;
import com.zhangyuan.modules.cms.dto.PageListItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ddd/cms")
public class CmsPageController {

    private static final Logger log = LoggerFactory.getLogger(CmsPageController.class);

    private final CmsApplicationService cmsApplicationService;

    public CmsPageController(CmsApplicationService cmsApplicationService) {
        this.cmsApplicationService = cmsApplicationService;
    }

    @GetMapping("/pages")
    public ApiResponse<com.zhangyuan.common.response.PageResponse<PageListItemResponse>> listPages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("Listing CMS pages, page={}, pageSize={}", page, pageSize);
        return ApiResponse.ok(cmsApplicationService.listPages(page, pageSize));
    }

    @PostMapping("/pages")
    public ApiResponse<PageDetailResponse> createPage(@RequestBody Map<String, Object> body) {
        String slug = (String) body.get("slug");
        String defaultLocale = (String) body.getOrDefault("defaultLocale", "zh-CN");
        String title = (String) body.getOrDefault("title", slug);
        String pageType = (String) body.getOrDefault("pageType", "custom");
        CreatePageRequest request = new CreatePageRequest(slug, title, defaultLocale, pageType);
        log.info("Creating CMS page with slug: {}", slug);
        return ApiResponse.ok(cmsApplicationService.createPage(request));
    }

    @GetMapping("/pages/{id}")
    public ApiResponse<PageDetailResponse> getPage(@PathVariable Long id) {
        log.info("Getting CMS page by id: {}", id);
        return ApiResponse.ok(cmsApplicationService.getPage(id));
    }

    @DeleteMapping("/pages/{id}")
    public ApiResponse<Void> deletePage(@PathVariable Long id) {
        log.info("Deleting CMS page: {}", id);
        cmsApplicationService.deletePage(id);
        log.info("CMS page deleted: {}", id);
        return ApiResponse.ok();
    }
}

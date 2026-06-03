package com.zhangyuan.modules.cms.adapter.in.rest;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.application.service.CmsApplicationService;
import com.zhangyuan.modules.cms.domain.model.CmsPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DDD 版 CMS 页面控制器，提供页面的增删改查接口。
 */
@RestController
@RequestMapping("/api/ddd/cms")
public class CmsPageController {

    private static final Logger log = LoggerFactory.getLogger(CmsPageController.class);

    private final CmsApplicationService cmsApplicationService;

    public CmsPageController(CmsApplicationService cmsApplicationService) {
        this.cmsApplicationService = cmsApplicationService;
    }

    /**
     * 获取所有 CMS 页面列表。
     *
     * @return 页面列表
     */
    @GetMapping("/pages")
    public ApiResponse<List<CmsPage>> listPages() {
        log.info("Listing all CMS pages");
        return ApiResponse.ok(cmsApplicationService.listAll());
    }

    /**
     * 创建新 CMS 页面。
     *
     * @param request 创建页面请求
     * @return 创建的页面
     */
    @PostMapping("/pages")
    public ApiResponse<CmsPage> createPage(@RequestBody CreatePageRequest request) {
        log.info("Creating CMS page with slug: {}", request.slug());
        CmsPage page = cmsApplicationService.createPage(request.slug(), request.defaultLocale(), request.createdBy());
        log.info("CMS page created: id={}, slug={}", page.getId(), page.getSlug());
        return ApiResponse.ok(page);
    }

    /**
     * 根据 ID 查询 CMS 页面。
     *
     * @param id 页面 ID
     * @return 页面信息
     */
    @GetMapping("/pages/{id}")
    public ApiResponse<CmsPage> getPage(@PathVariable Long id) {
        log.info("Getting CMS page by id: {}", id);
        return cmsApplicationService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "CMS page not found"));
    }

    /**
     * 删除指定 CMS 页面。
     *
     * @param id 页面 ID
     * @return 成功响应
     */
    @DeleteMapping("/pages/{id}")
    public ApiResponse<Void> deletePage(@PathVariable Long id) {
        log.info("Deleting CMS page: {}", id);
        cmsApplicationService.deletePage(id);
        log.info("CMS page deleted: {}", id);
        return ApiResponse.ok();
    }

    public record CreatePageRequest(String slug, String defaultLocale, Long createdBy) {}
}

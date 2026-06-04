package com.zhangyuan.modules.cms;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.dto.CreatePageRequest;
import com.zhangyuan.modules.cms.dto.PageDetailResponse;
import com.zhangyuan.modules.cms.dto.PageListItemResponse;
import com.zhangyuan.modules.cms.dto.PublishPageRequest;
import com.zhangyuan.modules.cms.dto.SaveDraftRequest;
import com.zhangyuan.modules.cms.dto.UpdatePageRequest;
import com.zhangyuan.modules.cms.dto.VersionResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 后台 CMS 页面管理控制器，提供页面的增删改查、草稿、版本、预览、发布和回滚接口。
 */
@RestController
@RequestMapping("/admin/cms/pages")
@SaCheckPermission("cms:manage")
public class CmsAdminController {

    private static final Logger log = LoggerFactory.getLogger(CmsAdminController.class);

    private final CmsService cmsService;

    public CmsAdminController(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    @GetMapping
    public ApiResponse<List<PageListItemResponse>> listPages() {
        log.info("Listing all CMS pages");
        return ApiResponse.ok(cmsService.listPages());
    }

    @PostMapping
    public ApiResponse<PageDetailResponse> createPage(@Valid @RequestBody CreatePageRequest request) {
        log.info("Creating CMS page: slug={}, title={}", request.slug(), request.title());
        return ApiResponse.ok(cmsService.createPage(request));
    }

    @GetMapping("/{pageId}")
    public ApiResponse<PageDetailResponse> getPage(@PathVariable Long pageId) {
        log.info("Getting CMS page: {}", pageId);
        return ApiResponse.ok(cmsService.getPage(pageId));
    }

    @PutMapping("/{pageId}")
    public ApiResponse<PageDetailResponse> updatePage(@PathVariable Long pageId, @Valid @RequestBody UpdatePageRequest request) {
        log.info("Updating CMS page: {}", pageId);
        return ApiResponse.ok(cmsService.updatePage(pageId, request));
    }

    @DeleteMapping("/{pageId}")
    public ApiResponse<Void> deletePage(@PathVariable Long pageId) {
        log.info("Deleting CMS page: {}", pageId);
        cmsService.deletePage(pageId);
        log.info("CMS page deleted: {}", pageId);
        return ApiResponse.ok();
    }

    @PutMapping("/{pageId}/translations/{locale}/draft")
    public ApiResponse<PageDetailResponse> saveDraft(@PathVariable Long pageId, @PathVariable String locale,
                                                     @Valid @RequestBody SaveDraftRequest request) {
        log.info("Saving draft for CMS page: {}, locale: {}", pageId, locale);
        return ApiResponse.ok(cmsService.saveDraft(pageId, locale, request));
    }

    @GetMapping("/{pageId}/translations/{locale}/draft")
    public ApiResponse<Map<String, Object>> getDraftVersion(@PathVariable Long pageId, @PathVariable String locale) {
        log.info("Getting draft version for CMS page: {}, locale: {}", pageId, locale);
        return ApiResponse.ok(cmsService.getDraftVersion(pageId, locale));
    }

    @GetMapping("/{pageId}/translations/{locale}/versions")
    public ApiResponse<List<VersionResponse>> listVersions(@PathVariable Long pageId, @PathVariable String locale) {
        log.info("Listing versions for CMS page: {}, locale: {}", pageId, locale);
        return ApiResponse.ok(cmsService.listVersions(pageId, locale));
    }

    @GetMapping("/{pageId}/preview")
    public ApiResponse<Map<String, Object>> preview(@PathVariable Long pageId,
                                                     @RequestParam(defaultValue = "zh-CN") String locale,
                                                     @RequestParam(required = false) Long versionId) {
        log.info("Previewing CMS page: {}, locale: {}, versionId: {}", pageId, locale, versionId);
        return ApiResponse.ok(cmsService.preview(pageId, locale, versionId));
    }

    @PostMapping("/{pageId}/translations/{locale}/rollback")
    public ApiResponse<PageDetailResponse> rollback(@PathVariable Long pageId, @PathVariable String locale,
                                                     @RequestBody PublishPageRequest request) {
        log.info("Rolling back CMS page: {}, locale: {}, versionId: {}", pageId, locale, request.versionId());
        return ApiResponse.ok(cmsService.rollback(pageId, locale, request));
    }

    @PostMapping("/{pageId}/translations/{locale}/publish")
    public ApiResponse<PageDetailResponse> publish(@PathVariable Long pageId, @PathVariable String locale,
                                                   @RequestBody(required = false) PublishPageRequest request) {
        log.info("Publishing CMS page: {}, locale: {}", pageId, locale);
        return ApiResponse.ok(cmsService.publish(pageId, locale, request));
    }
}

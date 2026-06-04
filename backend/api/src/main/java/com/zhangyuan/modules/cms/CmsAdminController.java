package com.zhangyuan.modules.cms;

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
public class CmsAdminController {

    private static final Logger log = LoggerFactory.getLogger(CmsAdminController.class);

    private final CmsService cmsService;

    public CmsAdminController(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    /**
     * 获取所有 CMS 页面列表。
     *
     * @return 页面列表项响应列表
     */
    @GetMapping
    public ApiResponse<List<PageListItemResponse>> listPages() {
        log.info("Listing all CMS pages");
        return ApiResponse.ok(cmsService.listPages());
    }

    /**
     * 创建新 CMS 页面。
     *
     * @param request 创建页面请求
     * @return 页面详情
     */
    @PostMapping
    public ApiResponse<PageDetailResponse> createPage(@Valid @RequestBody CreatePageRequest request) {
        log.info("Creating CMS page: slug={}, title={}", request.slug(), request.title());
        return ApiResponse.ok(cmsService.createPage(request));
    }

    /**
     * 根据 ID 查询 CMS 页面。
     *
     * @param pageId 页面 ID
     * @return 页面详情
     */
    @GetMapping("/{pageId}")
    public ApiResponse<PageDetailResponse> getPage(@PathVariable Long pageId) {
        log.info("Getting CMS page: {}", pageId);
        return ApiResponse.ok(cmsService.getPage(pageId));
    }

    /**
     * 更新 CMS 页面基本信息。
     *
     * @param pageId  页面 ID
     * @param request 更新页面请求
     * @return 更新后的页面详情
     */
    @PutMapping("/{pageId}")
    public ApiResponse<PageDetailResponse> updatePage(@PathVariable Long pageId, @Valid @RequestBody UpdatePageRequest request) {
        log.info("Updating CMS page: {}", pageId);
        return ApiResponse.ok(cmsService.updatePage(pageId, request));
    }

    /**
     * 删除 CMS 页面及相关翻译、版本、发布记录。
     *
     * @param pageId 页面 ID
     * @return 成功响应
     */
    @DeleteMapping("/{pageId}")
    public ApiResponse<Void> deletePage(@PathVariable Long pageId) {
        log.info("Deleting CMS page: {}", pageId);
        cmsService.deletePage(pageId);
        log.info("CMS page deleted: {}", pageId);
        return ApiResponse.ok();
    }

    /**
     * 保存草稿版本。
     *
     * @param pageId  页面 ID
     * @param locale  语言环境
     * @param request 保存草稿请求
     * @return 页面详情
     */
    @PutMapping("/{pageId}/translations/{locale}/draft")
    public ApiResponse<PageDetailResponse> saveDraft(@PathVariable Long pageId, @PathVariable String locale,
                                                     @Valid @RequestBody SaveDraftRequest request) {
        log.info("Saving draft for CMS page: {}, locale: {}", pageId, locale);
        return ApiResponse.ok(cmsService.saveDraft(pageId, locale, request));
    }

    /**
     * 获取当前草稿版本内容。
     *
     * @param pageId 页面 ID
     * @param locale 语言环境
     * @return 草稿版本内容
     */
    @GetMapping("/{pageId}/translations/{locale}/draft")
    public ApiResponse<Map<String, Object>> getDraftVersion(@PathVariable Long pageId, @PathVariable String locale) {
        log.info("Getting draft version for CMS page: {}, locale: {}", pageId, locale);
        return ApiResponse.ok(cmsService.getDraftVersion(pageId, locale));
    }

    /**
     * 获取页面的版本历史列表。
     *
     * @param pageId 页面 ID
     * @param locale 语言环境
     * @return 版本响应列表
     */
    @GetMapping("/{pageId}/translations/{locale}/versions")
    public ApiResponse<List<VersionResponse>> listVersions(@PathVariable Long pageId, @PathVariable String locale) {
        log.info("Listing versions for CMS page: {}, locale: {}", pageId, locale);
        return ApiResponse.ok(cmsService.listVersions(pageId, locale));
    }

    /**
     * 预览页面（可指定版本或使用当前草稿）。
     *
     * @param pageId   页面 ID
     * @param locale   语言环境
     * @param versionId 版本 ID（可选）
     * @return 预览内容
     */
    @GetMapping("/{pageId}/preview")
    public ApiResponse<Map<String, Object>> preview(@PathVariable Long pageId,
                                                     @RequestParam(defaultValue = "zh-CN") String locale,
                                                     @RequestParam(required = false) Long versionId) {
        log.info("Previewing CMS page: {}, locale: {}, versionId: {}", pageId, locale, versionId);
        return ApiResponse.ok(cmsService.preview(pageId, locale, versionId));
    }

    /**
     * 回滚到指定版本。
     *
     * @param pageId  页面 ID
     * @param locale  语言环境
     * @param request 发布请求（含版本 ID）
     * @return 页面详情
     */
    @PostMapping("/{pageId}/translations/{locale}/rollback")
    public ApiResponse<PageDetailResponse> rollback(@PathVariable Long pageId, @PathVariable String locale,
                                                     @RequestBody PublishPageRequest request) {
        log.info("Rolling back CMS page: {}, locale: {}, versionId: {}", pageId, locale, request.versionId());
        return ApiResponse.ok(cmsService.rollback(pageId, locale, request));
    }

    /**
     * 发布草稿版本。
     *
     * @param pageId  页面 ID
     * @param locale  语言环境
     * @param request 发布请求（可选）
     * @return 页面详情
     */
    @PostMapping("/{pageId}/translations/{locale}/publish")
    public ApiResponse<PageDetailResponse> publish(@PathVariable Long pageId, @PathVariable String locale,
                                                   @RequestBody(required = false) PublishPageRequest request) {
        log.info("Publishing CMS page: {}, locale: {}", pageId, locale);
        return ApiResponse.ok(cmsService.publish(pageId, locale, request));
    }
}

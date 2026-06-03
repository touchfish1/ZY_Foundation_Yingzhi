package com.zhangyuan.modules.cms;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.dto.CreatePageRequest;
import com.zhangyuan.modules.cms.dto.PageDetailResponse;
import com.zhangyuan.modules.cms.dto.PageListItemResponse;
import com.zhangyuan.modules.cms.dto.PublishPageRequest;
import com.zhangyuan.modules.cms.dto.SaveDraftRequest;
import com.zhangyuan.modules.cms.dto.VersionResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/cms/pages")
public class CmsAdminController {

    private final CmsService cmsService;

    public CmsAdminController(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    @GetMapping
    public ApiResponse<List<PageListItemResponse>> listPages() {
        return ApiResponse.ok(cmsService.listPages());
    }

    @PostMapping
    public ApiResponse<PageDetailResponse> createPage(@Valid @RequestBody CreatePageRequest request) {
        return ApiResponse.ok(cmsService.createPage(request));
    }

    @GetMapping("/{pageId}")
    public ApiResponse<PageDetailResponse> getPage(@PathVariable Long pageId) {
        return ApiResponse.ok(cmsService.getPage(pageId));
    }

    @PutMapping("/{pageId}/translations/{locale}/draft")
    public ApiResponse<PageDetailResponse> saveDraft(@PathVariable Long pageId, @PathVariable String locale,
                                                     @Valid @RequestBody SaveDraftRequest request) {
        return ApiResponse.ok(cmsService.saveDraft(pageId, locale, request));
    }

    @GetMapping("/{pageId}/translations/{locale}/draft")
    public ApiResponse<Map<String, Object>> getDraftVersion(@PathVariable Long pageId, @PathVariable String locale) {
        return ApiResponse.ok(cmsService.getDraftVersion(pageId, locale));
    }

    @GetMapping("/{pageId}/translations/{locale}/versions")
    public ApiResponse<List<VersionResponse>> listVersions(@PathVariable Long pageId, @PathVariable String locale) {
        return ApiResponse.ok(cmsService.listVersions(pageId, locale));
    }

    @PostMapping("/{pageId}/translations/{locale}/publish")
    public ApiResponse<PageDetailResponse> publish(@PathVariable Long pageId, @PathVariable String locale,
                                                   @RequestBody(required = false) PublishPageRequest request) {
        return ApiResponse.ok(cmsService.publish(pageId, locale, request));
    }
}

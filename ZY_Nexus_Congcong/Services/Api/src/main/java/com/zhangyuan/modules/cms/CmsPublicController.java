package com.zhangyuan.modules.cms;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.dto.RenderPageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开 CMS 页面控制器，提供页面渲染接口。
 */
@RestController
@RequestMapping("/api/cms/pages")
public class CmsPublicController {

    private static final Logger log = LoggerFactory.getLogger(CmsPublicController.class);

    private final CmsService cmsService;

    public CmsPublicController(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    /**
     * 渲染指定路径和语言环境的 CMS 页面。
     *
     * @param path   页面路径
     * @param locale 语言环境（可选，默认使用页面默认语言）
     * @return 渲染后的页面响应
     */
    @GetMapping("/render")
    public ApiResponse<RenderPageResponse> render(@RequestParam("path") String path,
                                                  @RequestParam(value = "locale", required = false) String locale) {
        log.info("Rendering CMS page: path={}, locale={}", path, locale);
        return ApiResponse.ok(cmsService.render(path, locale));
    }
}

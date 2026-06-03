package com.zhangyuan.modules.cms;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.cms.dto.RenderPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cms/pages")
public class CmsPublicController {

    private final CmsService cmsService;

    public CmsPublicController(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    @GetMapping("/render")
    public ApiResponse<RenderPageResponse> render(@RequestParam("path") String path,
                                                  @RequestParam(value = "locale", required = false) String locale) {
        return ApiResponse.ok(cmsService.render(path, locale));
    }
}

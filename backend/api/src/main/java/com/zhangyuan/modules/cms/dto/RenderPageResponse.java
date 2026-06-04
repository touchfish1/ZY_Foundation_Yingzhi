package com.zhangyuan.modules.cms.dto;

import java.util.Map;

public record RenderPageResponse(
        String path,
        String locale,
        String title,
        Map<String, Object> seo,
        String layout,
        Object blocks
) {
}

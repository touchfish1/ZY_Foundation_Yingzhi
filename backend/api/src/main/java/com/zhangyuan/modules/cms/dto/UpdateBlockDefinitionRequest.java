package com.zhangyuan.modules.cms.dto;

import java.util.Map;

public record UpdateBlockDefinitionRequest(
        String type,
        String name,
        Map<String, Object> schema,
        Map<String, Object> defaultProps,
        Integer sortOrder
) {
}

package com.zhangyuan.modules.cms.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PageContent {

    private final Map<String, Object> blocks;
    private final String layout;

    @SuppressWarnings("unchecked")
    public PageContent(Map<String, Object> content) {
        this.blocks = content != null ? Collections.unmodifiableMap(content) : Collections.emptyMap();
        this.layout = (String) (content != null ? content.getOrDefault("layout", "default") : "default");
    }

    @SuppressWarnings("unchecked")
    public PageContent(Object blocks) {
        if (blocks instanceof Map) {
            this.blocks = Collections.unmodifiableMap((Map<String, Object>) blocks);
            this.layout = (String) ((Map<String, Object>) blocks).getOrDefault("layout", "default");
        } else {
            this.blocks = Collections.emptyMap();
            this.layout = "default";
        }
    }

    public Map<String, Object> getBlocks() { return blocks; }
    public String getLayout() { return layout; }
}

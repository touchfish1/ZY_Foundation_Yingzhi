package com.zhangyuan.modules.cms.domain.model;

import java.util.Map;

public class CmsBlockDefinition {

    private Long id;
    private String type;
    private String name;
    private Map<String, Object> schemaJson;
    private Map<String, Object> defaultPropsJson;
    private boolean enabled;
    private int sortOrder;

    public CmsBlockDefinition(String type, String name, Map<String, Object> schemaJson,
                              Map<String, Object> defaultPropsJson, int sortOrder) {
        this.type = type;
        this.name = name;
        this.schemaJson = schemaJson;
        this.defaultPropsJson = defaultPropsJson;
        this.sortOrder = sortOrder;
        this.enabled = true;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public Map<String, Object> getSchemaJson() { return schemaJson; }
    public Map<String, Object> getDefaultPropsJson() { return defaultPropsJson; }
    public boolean isEnabled() { return enabled; }
    public int getSortOrder() { return sortOrder; }
}

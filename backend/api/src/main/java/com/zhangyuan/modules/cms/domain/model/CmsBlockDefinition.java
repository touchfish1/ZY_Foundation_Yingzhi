package com.zhangyuan.modules.cms.domain.model;

import java.time.Instant;
import java.util.Map;

public class CmsBlockDefinition {

    private Long id;
    private String type;
    private String name;
    private Map<String, Object> schemaJson;
    private Map<String, Object> defaultPropsJson;
    private boolean enabled;
    private int sortOrder;
    private Instant createdAt;
    private Instant updatedAt;

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
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

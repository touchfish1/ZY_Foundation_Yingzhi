package com.zhangyuan.modules.cms.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "cms_block_definition")
public class CmsBlockDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String type;

    @Column(nullable = false, length = 64)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> schemaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_props_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> defaultPropsJson;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected CmsBlockDefinition() {
    }

    public CmsBlockDefinition(String type, String name, Map<String, Object> schemaJson, Map<String, Object> defaultPropsJson, Integer sortOrder) {
        this.type = type;
        this.name = name;
        this.schemaJson = schemaJson;
        this.defaultPropsJson = defaultPropsJson;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getSchemaJson() {
        return schemaJson;
    }

    public Map<String, Object> getDefaultPropsJson() {
        return defaultPropsJson;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSchemaJson(Map<String, Object> schemaJson) {
        this.schemaJson = schemaJson;
    }

    public void setDefaultPropsJson(Map<String, Object> defaultPropsJson) {
        this.defaultPropsJson = defaultPropsJson;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}

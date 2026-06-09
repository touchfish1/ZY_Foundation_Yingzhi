package com.zhangyuan.modules.cms.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cms_page")
public class CmsPage {

    public static final String STATUS_ENABLED = "enabled";
    public static final String STATUS_DISABLED = "disabled";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "page_type", nullable = false, length = 64)
    private String pageType = "custom";

    @Column(name = "default_locale", nullable = false, length = 16)
    private String defaultLocale = "zh-CN";

    @Column(nullable = false, length = 32)
    private String status = STATUS_ENABLED;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected CmsPage() {
    }

    public CmsPage(String slug, String defaultLocale, String pageType, Long createdBy) {
        this.slug = normalizeSlug(slug);
        this.defaultLocale = defaultLocale;
        this.pageType = pageType != null ? pageType : "custom";
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getPageType() {
        return pageType;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public String getStatus() {
        return status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void disable() {
        this.status = STATUS_DISABLED;
    }

    public void enable() {
        this.status = STATUS_ENABLED;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public static String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return "/";
        }
        String normalized = slug.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }
}

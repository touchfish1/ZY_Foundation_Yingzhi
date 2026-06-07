package com.zhangyuan.modules.cms.domain.model;

import com.zhangyuan.common.dddframework.AggregateRoot;
import java.time.Instant;
import java.util.*;

public class CmsPage extends AggregateRoot<Long> {

    public static final String STATUS_ENABLED = "enabled";
    public static final String STATUS_DISABLED = "disabled";

    private String slug;
    private String pageType = "custom";
    private String defaultLocale;
    private boolean enabled;
    private List<PageTranslation> translations;
    private Long createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public CmsPage(String slug, String defaultLocale, String pageType, Long createdBy) {
        this.slug = normalizeSlug(slug);
        this.defaultLocale = defaultLocale;
        this.pageType = pageType != null ? pageType : "custom";
        this.createdBy = createdBy;
        this.enabled = true;
        this.translations = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public PageTranslation addTranslation(String locale, String title) {
        PageTranslation translation = new PageTranslation(locale, title);
        translations.add(translation);
        this.updatedAt = Instant.now();
        return translation;
    }

    public PageTranslation getTranslation(String locale) {
        return translations.stream()
                .filter(t -> t.getLocale().equals(locale))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Translation not found: " + locale));
    }

    public void changeSlug(String newSlug) {
        this.slug = normalizeSlug(newSlug);
        this.updatedAt = Instant.now();
    }

    public void disable() { this.enabled = false; }
    public void enable() { this.enabled = true; }

    public void changeType(String pageType) {
        this.pageType = pageType != null ? pageType : "custom";
        this.updatedAt = Instant.now();
    }

    public void changeDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
        this.updatedAt = Instant.now();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public String getStatus() {
        return enabled ? STATUS_ENABLED : STATUS_DISABLED;
    }

    public String getSlug() { return slug; }
    public String getPageType() { return pageType; }
    public String getDefaultLocale() { return defaultLocale; }
    public boolean isEnabled() { return enabled; }
    public List<PageTranslation> getTranslations() { return Collections.unmodifiableList(translations); }
    public Long getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) throw new IllegalArgumentException("Slug must not be blank");
        String normalized = slug.trim().toLowerCase();
        if (!normalized.startsWith("/")) normalized = "/" + normalized;
        if (normalized.length() > 1 && normalized.endsWith("/")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }
}

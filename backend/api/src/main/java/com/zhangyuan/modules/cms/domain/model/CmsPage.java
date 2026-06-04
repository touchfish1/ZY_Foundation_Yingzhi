package com.zhangyuan.modules.cms.domain.model;

import java.time.Instant;
import java.util.*;

public class CmsPage {

    private Long id;
    private String slug;
    private String defaultLocale;
    private boolean enabled;
    private List<PageTranslation> translations;
    private Long createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public CmsPage(String slug, String defaultLocale, Long createdBy) {
        this.slug = normalizeSlug(slug);
        this.defaultLocale = defaultLocale;
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

    public Long getId() { return id; }
    public String getSlug() { return slug; }
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

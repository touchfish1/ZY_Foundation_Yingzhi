package com.zhangyuan.modules.cms.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cms_page_translation")
public class CmsPageTranslation {

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PUBLISHED = "published";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(nullable = false, length = 16)
    private String locale;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description")
    private String seoDescription;

    @Column(name = "seo_keywords", length = 512)
    private String seoKeywords;

    @Column(name = "draft_version_id")
    private Long draftVersionId;

    @Column(name = "published_version_id")
    private Long publishedVersionId;

    @Column(nullable = false, length = 32)
    private String status = STATUS_DRAFT;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected CmsPageTranslation() {
    }

    public CmsPageTranslation(Long pageId, String locale, String title) {
        this.pageId = pageId;
        this.locale = locale;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public Long getPageId() {
        return pageId;
    }

    public String getLocale() {
        return locale;
    }

    public String getTitle() {
        return title;
    }

    public String getSeoTitle() {
        return seoTitle;
    }

    public String getSeoDescription() {
        return seoDescription;
    }

    public String getSeoKeywords() {
        return seoKeywords;
    }

    public Long getDraftVersionId() {
        return draftVersionId;
    }

    public Long getPublishedVersionId() {
        return publishedVersionId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateDraft(String title, String seoTitle, String seoDescription, String seoKeywords, Long draftVersionId) {
        this.title = title;
        this.seoTitle = seoTitle;
        this.seoDescription = seoDescription;
        this.seoKeywords = seoKeywords;
        this.draftVersionId = draftVersionId;
        this.updatedAt = Instant.now();
    }

    public void publish(Long versionId) {
        this.publishedVersionId = versionId;
        this.status = STATUS_PUBLISHED;
        this.updatedAt = Instant.now();
    }

    public void clearDraftVersionId() {
        this.draftVersionId = null;
        this.updatedAt = Instant.now();
    }

    public void clearPublishedVersionId() {
        this.publishedVersionId = null;
        this.updatedAt = Instant.now();
    }
}

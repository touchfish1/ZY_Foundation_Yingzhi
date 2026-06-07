package com.zhangyuan.modules.cms.domain.model;

import java.util.*;

public class PageTranslation {

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PUBLISHED = "published";

    private String locale;
    private String title;
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
    private PageContent draft;
    private PageContent published;
    private Long draftVersionId;
    private Long publishedVersionId;
    private String status;
    private List<PageVersion> versions;

    public PageTranslation(String locale, String title) {
        this.locale = locale;
        this.title = title;
        this.status = STATUS_DRAFT;
        this.versions = new ArrayList<>();
    }

    public PageVersion saveDraft(Map<String, Object> content, String remark) {
        int nextNo = (int) versions.stream().filter(v -> true).count() + 1;
        PageVersion version = new PageVersion(nextNo, content, remark);
        versions.add(version);
        this.draft = new PageContent(content);
        return version;
    }

    public void updateDraftInfo(String title, String seoTitle, String seoDescription,
                                 String seoKeywords, Long draftVersionId) {
        this.title = title;
        this.seoTitle = seoTitle;
        this.seoDescription = seoDescription;
        this.seoKeywords = seoKeywords;
        this.draftVersionId = draftVersionId;
    }

    public void publish(Long versionId) {
        this.publishedVersionId = versionId;
        this.status = STATUS_PUBLISHED;
    }

    public void updateSeo(String seoTitle, String seoDescription, String seoKeywords) {
        this.seoTitle = seoTitle;
        this.seoDescription = seoDescription;
        this.seoKeywords = seoKeywords;
    }

    public String getLocale() { return locale; }
    public String getTitle() { return title; }
    public String getSeoTitle() { return seoTitle; }
    public String getSeoDescription() { return seoDescription; }
    public String getSeoKeywords() { return seoKeywords; }
    public PageContent getDraft() { return draft; }
    public PageContent getPublished() { return published; }
    public Long getDraftVersionId() { return draftVersionId; }
    public Long getPublishedVersionId() { return publishedVersionId; }
    public String getStatus() { return status; }
    public List<PageVersion> getVersions() { return Collections.unmodifiableList(versions); }
}

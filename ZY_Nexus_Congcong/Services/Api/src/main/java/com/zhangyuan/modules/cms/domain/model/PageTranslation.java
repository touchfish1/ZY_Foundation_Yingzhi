package com.zhangyuan.modules.cms.domain.model;

import java.util.*;

public class PageTranslation {

    private String locale;
    private String title;
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
    private PageContent draft;
    private PageContent published;
    private Integer draftVersionNo;
    private Integer publishedVersionNo;
    private List<PageVersion> versions;

    public PageTranslation(String locale, String title) {
        this.locale = locale;
        this.title = title;
        this.versions = new ArrayList<>();
    }

    public PageVersion saveDraft(Map<String, Object> content, String remark) {
        int nextNo = (draftVersionNo != null ? draftVersionNo : 0) + 1;
        PageVersion version = new PageVersion(nextNo, content, remark);
        versions.add(version);
        this.draft = new PageContent(content);
        this.draftVersionNo = nextNo;
        return version;
    }

    public PageVersion publish(String remark) {
        if (draft == null) throw new IllegalStateException("No draft to publish");
        this.published = new PageContent(draft.getBlocks());
        this.publishedVersionNo = draftVersionNo;
        return versions.isEmpty() ? null : versions.get(versions.size() - 1);
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
    public Integer getDraftVersionNo() { return draftVersionNo; }
    public Integer getPublishedVersionNo() { return publishedVersionNo; }
    public List<PageVersion> getVersions() { return Collections.unmodifiableList(versions); }
}

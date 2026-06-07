package com.zhangyuan.modules.cms.domain.model;

import java.time.Instant;

public class CmsPublishRecord {

    private Long id;
    private Long pageId;
    private String locale;
    private Long versionId;
    private Long operatorId;
    private Instant publishedAt;
    private String remark;

    public CmsPublishRecord(Long pageId, String locale, Long versionId, Long operatorId, String remark) {
        this.pageId = pageId;
        this.locale = locale;
        this.versionId = versionId;
        this.operatorId = operatorId;
        this.publishedAt = Instant.now();
        this.remark = remark;
    }

    public Long getId() { return id; }
    public Long getPageId() { return pageId; }
    public String getLocale() { return locale; }
    public Long getVersionId() { return versionId; }
    public Long getOperatorId() { return operatorId; }
    public Instant getPublishedAt() { return publishedAt; }
    public String getRemark() { return remark; }
}

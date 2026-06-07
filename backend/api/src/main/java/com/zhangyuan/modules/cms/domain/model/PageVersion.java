package com.zhangyuan.modules.cms.domain.model;

import java.time.Instant;
import java.util.Map;

public class PageVersion {

    private Long id;
    private final int versionNo;
    private final Map<String, Object> content;
    private final String remark;
    private final Instant createdAt;

    public PageVersion(int versionNo, Map<String, Object> content, String remark) {
        this.versionNo = versionNo;
        this.content = content;
        this.remark = remark;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getVersionNo() { return versionNo; }
    public Map<String, Object> getContent() { return content; }
    public String getRemark() { return remark; }
    public Instant getCreatedAt() { return createdAt; }
}

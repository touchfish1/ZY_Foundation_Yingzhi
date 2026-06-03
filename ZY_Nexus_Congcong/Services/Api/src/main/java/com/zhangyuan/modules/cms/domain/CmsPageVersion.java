package com.zhangyuan.modules.cms.domain;

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
@Table(name = "cms_page_version")
public class CmsPageVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(nullable = false, length = 16)
    private String locale;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> contentJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_json", columnDefinition = "jsonb")
    private Map<String, Object> snapshotJson;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(length = 255)
    private String remark;

    protected CmsPageVersion() {
    }

    public CmsPageVersion(Long pageId, String locale, Integer versionNo, Map<String, Object> contentJson, Long createdBy, String remark) {
        this.pageId = pageId;
        this.locale = locale;
        this.versionNo = versionNo;
        this.contentJson = contentJson;
        this.createdBy = createdBy;
        this.remark = remark;
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

    public Integer getVersionNo() {
        return versionNo;
    }

    public Map<String, Object> getContentJson() {
        return contentJson;
    }

    public Map<String, Object> getSnapshotJson() {
        return snapshotJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getRemark() {
        return remark;
    }

    public void publishSnapshot(Map<String, Object> snapshotJson) {
        this.snapshotJson = snapshotJson;
    }
}

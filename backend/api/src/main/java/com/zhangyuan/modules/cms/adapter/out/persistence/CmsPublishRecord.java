package com.zhangyuan.modules.cms.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cms_publish_record")
public class CmsPublishRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(nullable = false, length = 16)
    private String locale;

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt = Instant.now();

    @Column(length = 255)
    private String remark;

    protected CmsPublishRecord() {
    }

    public CmsPublishRecord(Long pageId, String locale, Long versionId, Long operatorId, String remark) {
        this.pageId = pageId;
        this.locale = locale;
        this.versionId = versionId;
        this.operatorId = operatorId;
        this.remark = remark;
    }
}

package com.zhangyuan.modules.asset.domain.model;

import com.zhangyuan.common.dddframework.Entity;
import java.time.Instant;

public class AssetFile extends Entity<Long> {

    private String bucket;
    private String objectKey;
    private String originalName;
    private String contentType;
    private long sizeBytes;
    private String url;
    private Long createdBy;
    private Instant createdAt;

    public AssetFile(String bucket, String objectKey, String originalName, String contentType, long sizeBytes, Long createdBy) {
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucket() { return bucket; }
    public String getObjectKey() { return objectKey; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getUrl() { return url; }
    public Long getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}

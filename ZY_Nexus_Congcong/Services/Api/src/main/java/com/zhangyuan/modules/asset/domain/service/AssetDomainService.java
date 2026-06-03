package com.zhangyuan.modules.asset.domain.service;

import com.zhangyuan.modules.asset.domain.model.AssetFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 文件资产领域服务，包含文件资产的核心创建和 URL 构建逻辑。
 */
@Service
public class AssetDomainService {

    private static final Logger log = LoggerFactory.getLogger(AssetDomainService.class);

    /**
     * 创建文件资产领域对象。
     *
     * @param bucket        MinIO 桶名
     * @param objectKey     对象键
     * @param originalName  原始文件名
     * @param contentType   内容类型
     * @param sizeBytes     文件大小（字节）
     * @param createdBy     创建人 ID
     * @return 文件资产领域对象
     */
    public AssetFile createFile(String bucket, String objectKey, String originalName, String contentType, long sizeBytes, Long createdBy) {
        log.info("Domain creating asset file: originalName={}, objectKey={}", originalName, objectKey);
        return new AssetFile(bucket, objectKey, originalName, contentType, sizeBytes, createdBy);
    }

    /**
     * 构建文件访问 URL。
     *
     * @param endpoint  MinIO 端点
     * @param bucket    桶名
     * @param objectKey 对象键
     * @return 完整访问 URL
     */
    public String buildUrl(String endpoint, String bucket, String objectKey) {
        String url = endpoint + "/" + bucket + "/" + objectKey;
        log.debug("Asset URL built: {}", url);
        return url;
    }
}

package com.zhangyuan.modules.asset.application.service;

import com.zhangyuan.modules.asset.domain.model.AssetFile;
import com.zhangyuan.modules.asset.domain.repository.AssetRepository;
import com.zhangyuan.modules.asset.domain.service.AssetDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文件资产应用服务，负责文件资产创建的编排。
 */
@Service
public class AssetApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AssetApplicationService.class);

    private final AssetRepository assetRepository;
    private final AssetDomainService assetDomainService;

    public AssetApplicationService(AssetRepository assetRepository, AssetDomainService assetDomainService) {
        this.assetRepository = assetRepository;
        this.assetDomainService = assetDomainService;
    }

    /**
     * 创建文件资产记录。
     *
     * @param bucket        MinIO 桶名
     * @param objectKey     对象键
     * @param originalName  原始文件名
     * @param contentType   内容类型
     * @param sizeBytes     文件大小
     * @param createdBy     创建人 ID
     * @param url           文件访问 URL
     * @return 保存后的文件资产
     */
    @Transactional
    public AssetFile createFile(String bucket, String objectKey, String originalName, String contentType, long sizeBytes, Long createdBy, String url) {
        log.info("Creating asset file: originalName={}, bucket={}, sizeBytes={}", originalName, bucket, sizeBytes);
        AssetFile file = assetDomainService.createFile(bucket, objectKey, originalName, contentType, sizeBytes, createdBy);
        file.setUrl(url);
        AssetFile saved = assetRepository.save(file);
        log.info("Asset file created: id={}, url={}", saved.getId(), saved.getUrl());
        return saved;
    }

    /**
     * 获取所有资产文件列表。
     *
     * @return 资产文件列表
     */
    @Transactional(readOnly = true)
    public List<AssetFile> listAll() {
        return assetRepository.findAllOrderByCreatedAtDesc();
    }
}

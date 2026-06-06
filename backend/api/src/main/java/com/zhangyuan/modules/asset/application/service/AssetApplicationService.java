package com.zhangyuan.modules.asset.application.service;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.asset.domain.model.AssetFile;
import com.zhangyuan.modules.asset.domain.repository.AssetRepository;
import com.zhangyuan.modules.asset.domain.service.AssetDomainService;
import com.zhangyuan.modules.asset.dto.AssetFileInfo;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件资产应用服务，负责文件资产创建的编排。
 */
@Service
public class AssetApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AssetApplicationService.class);

    private final AssetRepository assetRepository;
    private final AssetDomainService assetDomainService;
    private final MinioClient minioClient;
    private final String minioEndpoint;
    private final String minioBucket;

    public AssetApplicationService(AssetRepository assetRepository, AssetDomainService assetDomainService,
            MinioClient minioClient, String minioEndpoint, String minioBucket) {
        this.assetRepository = assetRepository;
        this.assetDomainService = assetDomainService;
        this.minioClient = minioClient;
        this.minioEndpoint = minioEndpoint;
        this.minioBucket = minioBucket;
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

    /**
     * 获取所有文件列表，返回 DTO。
     *
     * @return 文件信息 DTO 列表
     */
    @Transactional(readOnly = true)
    public PageResponse<AssetFileInfo> listFiles(int page, int pageSize) {
        var pageResult = assetRepository.findPageByCreatedAtDesc(page, pageSize);
        List<AssetFileInfo> items = pageResult.items().stream()
                .map(file -> new AssetFileInfo(file.getId(), file.getUrl(), file.getOriginalName(), file.getContentType(), file.getSizeBytes()))
                .toList();
        return PageResponse.of(items, pageResult.page(), pageResult.pageSize(), pageResult.total());
    }

    /**
     * 上传文件到 MinIO 并保存文件记录。
     *
     * @param file   上传的文件
     * @param userId 上传用户 ID
     * @return 上传后的文件信息 DTO
     */
    @Transactional
    public AssetFileInfo upload(MultipartFile file, Long userId) throws Exception {
        log.info("Uploading file: originalName={}, userId={}", file.getOriginalFilename(), userId);

        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());
        if (!found) {
            log.info("Creating MinIO bucket: {}", minioBucket);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
        }

        String originalName = file.getOriginalFilename();
        String objectKey = assetDomainService.generateObjectKey(originalName);
        String contentType = file.getContentType();
        long size = file.getSize();

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioBucket)
                .object(objectKey)
                .stream(file.getInputStream(), size, -1)
                .contentType(contentType)
                .build());

        String url = assetDomainService.buildUrl(minioEndpoint, minioBucket, objectKey);
        AssetFile saved = createFile(minioBucket, objectKey, originalName, contentType, size, userId, url);

        return new AssetFileInfo(saved.getId(), saved.getUrl(), saved.getOriginalName(), saved.getContentType(), saved.getSizeBytes());
    }
}

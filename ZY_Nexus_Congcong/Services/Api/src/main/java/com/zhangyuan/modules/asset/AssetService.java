package com.zhangyuan.modules.asset;

import com.zhangyuan.modules.asset.domain.AssetFile;
import com.zhangyuan.modules.asset.dto.AssetFileInfo;
import com.zhangyuan.modules.asset.repository.AssetFileRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 文件资产服务，提供文件的存储（MinIO）和元数据管理功能。
 */
@Service
public class AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);

    private final MinioClient minioClient;
    private final String minioEndpoint;
    private final String minioBucket;
    private final AssetFileRepository assetFileRepository;

    public AssetService(MinioClient minioClient, String minioEndpoint, String minioBucket, AssetFileRepository assetFileRepository) {
        this.minioClient = minioClient;
        this.minioEndpoint = minioEndpoint;
        this.minioBucket = minioBucket;
        this.assetFileRepository = assetFileRepository;
    }

    /**
     * 获取所有文件列表，按创建时间降序排列。
     *
     * @return 文件信息列表
     */
    @Transactional(readOnly = true)
    public List<AssetFileInfo> listFiles() {
        return assetFileRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(file -> new AssetFileInfo(file.getId(), file.getUrl(), file.getOriginalName(), file.getContentType(), file.getSizeBytes()))
                .toList();
    }

    /**
     * 上传文件到 MinIO 并保存文件记录。
     *
     * @param file   上传的文件
     * @param userId 上传用户 ID
     * @return 上传后的文件信息
     */
    @Transactional
    public AssetFileInfo upload(MultipartFile file, Long userId) throws Exception {
        log.info("Uploading file: originalName={}, userId={}", file.getOriginalFilename(), userId);
        // 检查 MinIO 桶是否存在，不存在则创建
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build());
        if (!found) {
            log.info("Creating MinIO bucket: {}", minioBucket);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
        }

        String originalName = file.getOriginalFilename();
        // 使用 UUID 作为目录防止文件名冲突
        String objectKey = UUID.randomUUID() + "/" + originalName;
        String contentType = file.getContentType();
        long size = file.getSize();

        // 上传文件到 MinIO
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioBucket)
                .object(objectKey)
                .stream(file.getInputStream(), size, -1)
                .contentType(contentType)
                .build());

        // 构建文件访问 URL
        String url = minioEndpoint + "/" + minioBucket + "/" + objectKey;

        AssetFile assetFile = new AssetFile(minioBucket, objectKey, originalName, contentType, size, url, userId, Instant.now());
        assetFile = assetFileRepository.save(assetFile);
        log.info("File uploaded: id={}, url={}", assetFile.getId(), assetFile.getUrl());

        return new AssetFileInfo(assetFile.getId(), assetFile.getUrl(), assetFile.getOriginalName(), assetFile.getContentType(), assetFile.getSizeBytes());
    }
}

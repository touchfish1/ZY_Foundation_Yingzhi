package com.zhangyuan.modules.asset.adapter.out.persistence;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.asset.domain.model.AssetFile;
import com.zhangyuan.modules.asset.domain.repository.AssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaAssetRepository implements AssetRepository {

    private final AssetFileJpaRepository jpaRepository;

    public JpaAssetRepository(AssetFileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AssetFile> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AssetFile> findAllOrderByCreatedAtDesc() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResponse<AssetFile> findPageByCreatedAtDesc(int page, int pageSize) {
        Page<AssetFileEntity> pageResult = jpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, pageSize));
        List<AssetFile> items = pageResult.getContent().stream()
                .map(this::toDomain)
                .toList();
        return PageResponse.of(items, page, pageSize, pageResult.getTotalElements());
    }

    @Override
    public AssetFile save(AssetFile asset) {
        AssetFileEntity entity;
        if (asset.getId() != null) {
            entity = jpaRepository.findById(asset.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + asset.getId()));
            entity.setUrl(asset.getUrl());
        } else {
            entity = new AssetFileEntity(
                    asset.getBucket(),
                    asset.getObjectKey(),
                    asset.getOriginalName(),
                    asset.getContentType(),
                    asset.getSizeBytes(),
                    asset.getUrl(),
                    asset.getCreatedBy(),
                    asset.getCreatedAt()
            );
        }
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private AssetFile toDomain(AssetFileEntity entity) {
        AssetFile file = new AssetFile(
                entity.getBucket(),
                entity.getObjectKey(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getCreatedBy()
        );
        file.setId(entity.getId());
        file.setUrl(entity.getUrl());
        return file;
    }
}

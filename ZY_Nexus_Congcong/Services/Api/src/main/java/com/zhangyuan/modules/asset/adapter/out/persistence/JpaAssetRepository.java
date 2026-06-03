package com.zhangyuan.modules.asset.adapter.out.persistence;

import com.zhangyuan.modules.asset.domain.model.AssetFile;
import com.zhangyuan.modules.asset.domain.repository.AssetRepository;
import com.zhangyuan.modules.asset.repository.AssetFileRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaAssetRepository implements AssetRepository {

    private final AssetFileRepository jpaRepository;

    public JpaAssetRepository(AssetFileRepository jpaRepository) {
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
    public AssetFile save(AssetFile asset) {
        com.zhangyuan.modules.asset.domain.AssetFile entity = new com.zhangyuan.modules.asset.domain.AssetFile(
                asset.getBucket(),
                asset.getObjectKey(),
                asset.getOriginalName(),
                asset.getContentType(),
                asset.getSizeBytes(),
                asset.getUrl(),
                asset.getCreatedBy(),
                asset.getCreatedAt()
        );
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private AssetFile toDomain(com.zhangyuan.modules.asset.domain.AssetFile entity) {
        AssetFile file = new AssetFile(
                entity.getBucket(),
                entity.getObjectKey(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getCreatedBy()
        );
        file.setUrl(entity.getUrl());
        return file;
    }
}

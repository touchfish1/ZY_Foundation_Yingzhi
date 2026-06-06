package com.zhangyuan.modules.asset.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetFileJpaRepository extends JpaRepository<AssetFileEntity, Long> {

    List<AssetFileEntity> findAllByOrderByCreatedAtDesc();

    Page<AssetFileEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

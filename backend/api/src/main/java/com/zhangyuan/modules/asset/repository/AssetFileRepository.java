package com.zhangyuan.modules.asset.repository;

import com.zhangyuan.modules.asset.domain.AssetFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetFileRepository extends JpaRepository<AssetFile, Long> {

    List<AssetFile> findAllByOrderByCreatedAtDesc();
}

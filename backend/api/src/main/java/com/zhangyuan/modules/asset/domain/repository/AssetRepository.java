package com.zhangyuan.modules.asset.domain.repository;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.asset.domain.model.AssetFile;
import java.util.List;
import java.util.Optional;

public interface AssetRepository {

    Optional<AssetFile> findById(Long id);

    List<AssetFile> findAllOrderByCreatedAtDesc();

    PageResponse<AssetFile> findPageByCreatedAtDesc(int page, int pageSize);

    AssetFile save(AssetFile asset);
}

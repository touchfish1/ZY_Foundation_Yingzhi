package com.zhangyuan.modules.cms.domain.repository;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.cms.domain.model.CmsPage;
import java.util.List;
import java.util.Optional;

public interface CmsPageRepository {

    Optional<CmsPage> findById(Long id);

    Optional<CmsPage> findBySlug(String slug);

    List<CmsPage> findAll();

    PageResponse<CmsPage> findAll(int page, int pageSize);

    CmsPage save(CmsPage page);

    void deleteById(Long id);

    boolean existsBySlug(String slug);

    List<CmsPage> findByPageTypeAndStatus(String pageType, String status);

    PageResponse<CmsPage> findByPageTypeAndStatus(String pageType, String status, int page, int pageSize);
}

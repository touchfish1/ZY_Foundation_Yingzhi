package com.zhangyuan.modules.cms.repository;

import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {

    Optional<CmsPage> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<CmsPage> findByPageTypeAndStatus(String pageType, String status, Pageable pageable);

    @Query("SELECT DISTINCT p FROM CmsPage p LEFT JOIN CmsPageTranslation t ON t.pageId = p.id WHERE LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<CmsPage> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

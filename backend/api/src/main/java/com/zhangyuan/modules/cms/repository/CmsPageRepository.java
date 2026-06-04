package com.zhangyuan.modules.cms.repository;

import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {

    Optional<CmsPage> findBySlug(String slug);

    boolean existsBySlug(String slug);
}

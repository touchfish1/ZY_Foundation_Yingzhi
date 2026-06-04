package com.zhangyuan.modules.cms.repository;

import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPageTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CmsPageTranslationRepository extends JpaRepository<CmsPageTranslation, Long> {

    Optional<CmsPageTranslation> findByPageIdAndLocale(Long pageId, String locale);

    List<CmsPageTranslation> findByPageId(Long pageId);

    void deleteByPageId(Long pageId);
}

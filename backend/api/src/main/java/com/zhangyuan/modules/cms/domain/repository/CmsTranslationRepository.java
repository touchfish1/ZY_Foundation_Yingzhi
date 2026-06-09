package com.zhangyuan.modules.cms.domain.repository;

import com.zhangyuan.modules.cms.domain.model.PageTranslation;

import java.util.List;
import java.util.Optional;

public interface CmsTranslationRepository {

    PageTranslation create(Long pageId, String locale, String title);

    /**
     * Persist changes made to a PageTranslation domain object.
     * Requires pageId because the domain model does not carry an entity ID.
     */
    PageTranslation save(Long pageId, PageTranslation translation);

    Optional<PageTranslation> findByPageIdAndLocale(Long pageId, String locale);

    List<PageTranslation> findByPageId(Long pageId);

    void deleteByPageId(Long pageId);
}

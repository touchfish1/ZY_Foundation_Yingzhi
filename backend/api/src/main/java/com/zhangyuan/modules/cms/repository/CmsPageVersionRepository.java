package com.zhangyuan.modules.cms.repository;

import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPageVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CmsPageVersionRepository extends JpaRepository<CmsPageVersion, Long> {

    Optional<CmsPageVersion> findFirstByPageIdAndLocaleOrderByVersionNoDesc(Long pageId, String locale);
    List<CmsPageVersion> findByPageIdAndLocaleOrderByVersionNoDesc(Long pageId, String locale);

    Page<CmsPageVersion> findByPageIdAndLocaleOrderByVersionNoDesc(Long pageId, String locale, Pageable pageable);

    void deleteByPageIdAndLocale(Long pageId, String locale);
}

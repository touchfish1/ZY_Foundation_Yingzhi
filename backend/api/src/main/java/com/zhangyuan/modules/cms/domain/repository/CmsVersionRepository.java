package com.zhangyuan.modules.cms.domain.repository;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.cms.domain.model.PageVersion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CmsVersionRepository {

    /**
     * Create a new version entity and persist it.
     * Returns the created version with its generated ID.
     */
    PageVersion createAndSave(Long pageId, String locale, int versionNo,
                              Map<String, Object> content, Long operatorId, String remark);

    PageVersion save(PageVersion version);

    Optional<PageVersion> findById(Long id);

    Optional<PageVersion> findLatestByPageIdAndLocale(Long pageId, String locale);

    List<PageVersion> findByPageIdAndLocale(Long pageId, String locale);

    PageResponse<PageVersion> findByPageIdAndLocale(Long pageId, String locale, int page, int pageSize);

    Map<String, Object> getContentJson(Long versionId);

    Map<String, Object> getSnapshotJson(Long versionId);

    void publishSnapshot(Long versionId, Map<String, Object> snapshot);

    boolean versionBelongsToPage(Long versionId, Long pageId, String locale);

    void deleteByPageIdAndLocale(Long pageId, String locale);
}

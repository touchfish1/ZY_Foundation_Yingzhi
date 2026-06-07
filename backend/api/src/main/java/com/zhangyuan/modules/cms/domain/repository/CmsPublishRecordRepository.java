package com.zhangyuan.modules.cms.domain.repository;

import com.zhangyuan.modules.cms.domain.model.CmsPublishRecord;

public interface CmsPublishRecordRepository {

    CmsPublishRecord save(CmsPublishRecord record);

    void deleteByPageIdAndLocale(Long pageId, String locale);
}

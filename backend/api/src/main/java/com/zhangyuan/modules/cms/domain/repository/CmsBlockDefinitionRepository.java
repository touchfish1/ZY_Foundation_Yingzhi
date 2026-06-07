package com.zhangyuan.modules.cms.domain.repository;

import com.zhangyuan.modules.cms.domain.model.CmsBlockDefinition;

import java.util.List;

public interface CmsBlockDefinitionRepository {

    List<CmsBlockDefinition> findEnabled();
}

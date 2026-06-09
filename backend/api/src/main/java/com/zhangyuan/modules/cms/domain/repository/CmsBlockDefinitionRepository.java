package com.zhangyuan.modules.cms.domain.repository;

import com.zhangyuan.modules.cms.domain.model.CmsBlockDefinition;

import java.util.List;
import java.util.Optional;

public interface CmsBlockDefinitionRepository {

    List<CmsBlockDefinition> findEnabled();

    Optional<CmsBlockDefinition> findById(Long id);

    Optional<CmsBlockDefinition> findByType(String type);

    CmsBlockDefinition save(CmsBlockDefinition blockDefinition);
}

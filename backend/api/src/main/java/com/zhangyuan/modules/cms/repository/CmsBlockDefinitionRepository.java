package com.zhangyuan.modules.cms.repository;

import com.zhangyuan.modules.cms.adapter.out.persistence.CmsBlockDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CmsBlockDefinitionRepository extends JpaRepository<CmsBlockDefinition, Long> {

    Optional<CmsBlockDefinition> findByType(String type);
    List<CmsBlockDefinition> findByEnabledTrueOrderBySortOrderAsc();
}

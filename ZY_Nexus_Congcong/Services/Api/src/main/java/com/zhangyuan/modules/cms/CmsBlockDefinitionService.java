package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CmsBlockDefinitionService {

    private final CmsBlockDefinitionRepository repository;

    public CmsBlockDefinitionService(CmsBlockDefinitionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BlockDefinitionResponse> listEnabled() {
        return repository.findByEnabledTrueOrderBySortOrderAsc().stream()
                .map(block -> new BlockDefinitionResponse(block.getType(), block.getName(), block.getSchemaJson(), block.getDefaultPropsJson()))
                .toList();
    }
}

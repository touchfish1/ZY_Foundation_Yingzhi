package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CMS 区块定义服务，提供已启用区块定义的查询功能。
 */
@Service
public class CmsBlockDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(CmsBlockDefinitionService.class);

    private final CmsBlockDefinitionRepository repository;

    public CmsBlockDefinitionService(CmsBlockDefinitionRepository repository) {
        this.repository = repository;
    }

    /**
     * 获取所有已启用的区块定义列表。
     *
     * @return 区块定义响应列表
     */
    @Transactional(readOnly = true)
    public List<BlockDefinitionResponse> listEnabled() {
        log.info("Listing enabled block definitions");
        return repository.findByEnabledTrueOrderBySortOrderAsc().stream()
                .map(block -> new BlockDefinitionResponse(block.getType(), block.getName(), block.getSchemaJson(), block.getDefaultPropsJson()))
                .toList();
    }
}

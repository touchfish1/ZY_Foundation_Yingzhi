package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.adapter.out.persistence.CmsBlockDefinition;
import com.zhangyuan.modules.cms.application.service.CmsApplicationService;
import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CmsBlockDefinitionServiceTest {

    private final CmsBlockDefinitionRepository blockDefinitionRepository = mock(CmsBlockDefinitionRepository.class);
    private final CmsPageRepository pageRepository = mock(CmsPageRepository.class);
    private final CmsPageTranslationRepository translationRepository = mock(CmsPageTranslationRepository.class);
    private final CmsPageVersionRepository versionRepository = mock(CmsPageVersionRepository.class);
    private final CmsPublishRecordRepository publishRecordRepository = mock(CmsPublishRecordRepository.class);
    private final ProductApplicationService productService = mock(ProductApplicationService.class);
    private final CmsApplicationService service = CmsServiceTestFactory.create(pageRepository, translationRepository, versionRepository, publishRecordRepository, blockDefinitionRepository, productService);

    @Test
    void listEnabledReturnsOnlyEnabledBlocks() {
        CmsBlockDefinition def = new CmsBlockDefinition("hero", "Hero Block",
                Map.of("fields", List.of(Map.of("key", "title", "label", "Title", "type", "text"))),
                Map.of("title", "Default Title"), 1);
        when(blockDefinitionRepository.findByEnabledTrueOrderBySortOrderAsc()).thenReturn(List.of(def));

        List<BlockDefinitionResponse> result = service.listEnabledBlockDefinitions();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().type()).isEqualTo("hero");
        verify(blockDefinitionRepository).findByEnabledTrueOrderBySortOrderAsc();
    }

    @Test
    void listEnabledReturnsEmptyWhenNone() {
        when(blockDefinitionRepository.findByEnabledTrueOrderBySortOrderAsc()).thenReturn(List.of());

        List<BlockDefinitionResponse> result = service.listEnabledBlockDefinitions();

        assertThat(result).isEmpty();
    }
}

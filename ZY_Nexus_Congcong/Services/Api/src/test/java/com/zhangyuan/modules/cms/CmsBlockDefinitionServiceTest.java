package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.domain.CmsBlockDefinition;
import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CmsBlockDefinitionServiceTest {

    private final CmsBlockDefinitionRepository repository = mock(CmsBlockDefinitionRepository.class);
    private final CmsBlockDefinitionService service = new CmsBlockDefinitionService(repository);

    @Test
    void listEnabledReturnsOnlyEnabledBlocks() {
        CmsBlockDefinition def = new CmsBlockDefinition("hero", "Hero Block",
                Map.of("fields", List.of(Map.of("key", "title", "label", "Title", "type", "text"))),
                Map.of("title", "Default Title"), 1);
        when(repository.findByEnabledTrueOrderBySortOrderAsc()).thenReturn(List.of(def));

        List<BlockDefinitionResponse> result = service.listEnabled();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().type()).isEqualTo("hero");
        verify(repository).findByEnabledTrueOrderBySortOrderAsc();
    }

    @Test
    void listEnabledReturnsEmptyWhenNone() {
        when(repository.findByEnabledTrueOrderBySortOrderAsc()).thenReturn(List.of());

        List<BlockDefinitionResponse> result = service.listEnabled();

        assertThat(result).isEmpty();
    }
}

package com.zhangyuan.modules.cms.application.service;

import com.zhangyuan.modules.cms.domain.model.CmsPage;
import com.zhangyuan.modules.cms.domain.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.domain.service.CmsDomainService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CmsApplicationServiceTest {

    private final CmsPageRepository repository = mock(CmsPageRepository.class);
    private final CmsDomainService domainService = new CmsDomainService();
    private final CmsApplicationService service = new CmsApplicationService(repository, domainService);

    @Test
    void createPageSavesAndReturns() {
        when(repository.existsBySlug("/test")).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CmsPage result = service.createPage("/test", "zh-CN", 1L);

        assertThat(result.getSlug()).isEqualTo("/test");
        verify(repository).save(any());
    }

    @Test
    void listAllDelegates() {
        service.listAll();
        verify(repository).findAll();
    }
}

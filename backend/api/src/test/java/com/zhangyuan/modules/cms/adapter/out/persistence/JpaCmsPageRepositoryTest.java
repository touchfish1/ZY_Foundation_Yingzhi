package com.zhangyuan.modules.cms.adapter.out.persistence;

import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpaCmsPageRepositoryTest {

    private final CmsPageRepository jpaRepository = mock(CmsPageRepository.class);
    private final JpaCmsPageRepository repository = new JpaCmsPageRepository(jpaRepository);

    @Test
    void findBySlugDelegates() {
        com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage entity = new com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage("/plans", "zh-CN", "custom", 1L);
        when(jpaRepository.findBySlug("/plans")).thenReturn(Optional.of(entity));

        Optional<com.zhangyuan.modules.cms.domain.model.CmsPage> result = repository.findBySlug("/plans");

        assertThat(result).isPresent();
        assertThat(result.get().getSlug()).isEqualTo("/plans");
    }
}

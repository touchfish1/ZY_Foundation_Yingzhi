package com.zhangyuan.modules.asset.application.service;

import com.zhangyuan.modules.asset.domain.model.AssetFile;
import com.zhangyuan.modules.asset.domain.repository.AssetRepository;
import com.zhangyuan.modules.asset.domain.service.AssetDomainService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetApplicationServiceTest {

    private final AssetRepository repository = mock(AssetRepository.class);
    private final AssetDomainService domainService = new AssetDomainService();
    private final AssetApplicationService service = new AssetApplicationService(repository, domainService);

    @Test
    void createFileSaves() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AssetFile result = service.createFile("bucket", "key", "file.png", "image/png", 1024L, 1L, "url");

        assertThat(result.getOriginalName()).isEqualTo("file.png");
        verify(repository).save(any());
    }

    @Test
    void listAllDelegates() {
        service.listAll();
        verify(repository).findAllOrderByCreatedAtDesc();
    }
}

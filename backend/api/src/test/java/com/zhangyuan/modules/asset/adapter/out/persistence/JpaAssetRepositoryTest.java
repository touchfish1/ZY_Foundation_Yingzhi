package com.zhangyuan.modules.asset.adapter.out.persistence;

import com.zhangyuan.modules.asset.adapter.out.persistence.AssetFileEntity;
import com.zhangyuan.modules.asset.adapter.out.persistence.AssetFileJpaRepository;
import com.zhangyuan.modules.asset.domain.model.AssetFile;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpaAssetRepositoryTest {

    private final AssetFileJpaRepository jpaRepository = mock(AssetFileJpaRepository.class);
    private final JpaAssetRepository repository = new JpaAssetRepository(jpaRepository);

    @Test
    void findByIdDelegates() {
        AssetFileEntity entity = new AssetFileEntity(
                "bucket", "key", "file.png", "image/png", 1024L, "url", 1L, java.time.Instant.now());
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<AssetFile> result = repository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getOriginalName()).isEqualTo("file.png");
    }
}

package com.zhangyuan.modules.asset;

import com.zhangyuan.modules.asset.domain.AssetFile;
import com.zhangyuan.modules.asset.dto.AssetFileInfo;
import com.zhangyuan.modules.asset.repository.AssetFileRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetServiceTest {

    private final MinioClient minioClient = mock(MinioClient.class);
    private final String minioEndpoint = "http://localhost:9000";
    private final String minioBucket = "zhangyuan-assets";
    private final AssetFileRepository assetFileRepository = mock(AssetFileRepository.class);
    private final AssetService assetService = new AssetService(minioClient, minioEndpoint, minioBucket, assetFileRepository);

    @Test
    void listFilesReturnsAll() {
        AssetFile file = new AssetFile("zhangyuan-assets", "abc/photo.png", "photo.png", "image/png",
                1024L, "http://localhost:9000/zhangyuan-assets/abc/photo.png", 1L, Instant.now());
        when(assetFileRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(file));

        List<AssetFileInfo> files = assetService.listFiles();

        assertThat(files).hasSize(1);
        assertThat(files.getFirst().originalName()).isEqualTo("photo.png");
        assertThat(files.getFirst().contentType()).isEqualTo("image/png");
        assertThat(files.getFirst().sizeBytes()).isEqualTo(1024L);
    }

    @Test
    void listFilesReturnsEmptyWhenNone() {
        when(assetFileRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<AssetFileInfo> files = assetService.listFiles();

        assertThat(files).isEmpty();
    }
}

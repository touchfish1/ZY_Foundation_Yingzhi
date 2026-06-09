package com.zhangyuan.modules.asset.application.service;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.asset.domain.model.AssetFile;
import com.zhangyuan.modules.asset.domain.repository.AssetRepository;
import com.zhangyuan.modules.asset.domain.service.AssetDomainService;
import com.zhangyuan.modules.asset.dto.AssetFileInfo;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssetApplicationServiceTest {

    private final AssetRepository assetRepository = mock(AssetRepository.class);
    private final AssetDomainService assetDomainService = mock(AssetDomainService.class);
    private final MinioClient minioClient = mock(MinioClient.class);
    private AssetApplicationService service;

    @BeforeEach
    void setUp() {
        service = new AssetApplicationService(assetRepository, assetDomainService,
                minioClient, "http://localhost:9000", "test-bucket");
    }

    @Test
    void createFile_shouldSaveAndReturn() {
        when(assetDomainService.createFile("test-bucket", "key.jpg", "photo.jpg", "image/jpeg", 1024L, 1L))
                .thenReturn(new AssetFile("test-bucket", "key.jpg", "photo.jpg", "image/jpeg", 1024L, 1L));
        when(assetRepository.save(any())).thenAnswer(i -> {
            AssetFile f = i.getArgument(0);
            f.setId(99L);
            return f;
        });

        AssetFile result = service.createFile("test-bucket", "key.jpg", "photo.jpg", "image/jpeg", 1024L, 1L, "http://localhost:9000/test-bucket/key.jpg");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getUrl()).contains("key.jpg");
        verify(assetRepository).save(any());
    }

    @Test
    void listFiles_shouldReturnPage() {
        AssetFile file1 = new AssetFile("bucket", "k1", "a.jpg", "image/jpeg", 100L, 1L);
        file1.setId(1L);
        file1.setUrl("http://localhost:9000/bucket/k1");
        when(assetRepository.findPageByCreatedAtDesc(1, 20))
                .thenReturn(PageResponse.of(List.of(file1), 1, 20, 1));

        PageResponse<AssetFileInfo> result = service.listFiles(1, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).originalName()).isEqualTo("a.jpg");
    }

    @Test
    void listFiles_whenEmpty_shouldReturnEmpty() {
        when(assetRepository.findPageByCreatedAtDesc(1, 20))
                .thenReturn(PageResponse.of(List.of(), 1, 20, 0));

        PageResponse<AssetFileInfo> result = service.listFiles(1, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
    }

    @Test
    void upload_shouldCreateFileAndReturnInfo() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(2048L);
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("data".getBytes()));

        when(minioClient.bucketExists(any())).thenReturn(true);
        when(minioClient.putObject(any())).thenReturn(null);

        when(assetDomainService.generateObjectKey("test.jpg")).thenReturn("obj/test.jpg");
        when(assetDomainService.buildUrl("http://localhost:9000", "test-bucket", "obj/test.jpg"))
                .thenReturn("http://localhost:9000/test-bucket/obj/test.jpg");
        when(assetDomainService.createFile("test-bucket", "obj/test.jpg", "test.jpg", "image/jpeg", 2048L, 1L))
                .thenAnswer(i -> {
                    AssetFile f = new AssetFile("test-bucket", "obj/test.jpg", "test.jpg", "image/jpeg", 2048L, 1L);
                    f.setId(42L);
                    f.setUrl("http://localhost:9000/test-bucket/obj/test.jpg");
                    return f;
                });
        when(assetRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AssetFileInfo info = service.upload(file, 1L);

        assertThat(info.id()).isEqualTo(42L);
        assertThat(info.originalName()).isEqualTo("test.jpg");
        assertThat(info.contentType()).isEqualTo("image/jpeg");
        assertThat(info.sizeBytes()).isEqualTo(2048L);
        verify(minioClient).putObject(any());
    }

    @Test
    void upload_whenBucketNotExists_shouldCreateBucket() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(512L);
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("pdf".getBytes()));

        when(minioClient.bucketExists(any())).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any());
        when(minioClient.putObject(any())).thenReturn(null);

        when(assetDomainService.generateObjectKey("doc.pdf")).thenReturn("obj/doc.pdf");
        when(assetDomainService.buildUrl("http://localhost:9000", "test-bucket", "obj/doc.pdf"))
                .thenReturn("http://localhost:9000/test-bucket/obj/doc.pdf");
        when(assetDomainService.createFile(any(), any(), any(), any(), anyLong(), anyLong()))
                .thenAnswer(i -> {
                    AssetFile f = new AssetFile(i.getArgument(0), i.getArgument(1), i.getArgument(2),
                            i.getArgument(3), i.getArgument(4), i.getArgument(5));
                    f.setId(1L);
                    f.setUrl("http://localhost:9000/test-bucket/obj/doc.pdf");
                    return f;
                });
        when(assetRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.upload(file, 1L);

        verify(minioClient).makeBucket(any());
        verify(minioClient).putObject(any());
    }
}

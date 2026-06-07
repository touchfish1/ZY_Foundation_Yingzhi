package com.zhangyuan.common.operationlog;

import com.zhangyuan.common.operationlog.adapter.out.persistence.OperationLogEntity;
import com.zhangyuan.common.operationlog.adapter.out.persistence.OperationLogJpaRepository;
import com.zhangyuan.modules.asset.adapter.out.persistence.AssetFileJpaRepository;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.order.adapter.out.persistence.OrderMainEntityRepository;
import com.zhangyuan.modules.payment.adapter.out.persistence.PaymentTransactionJpaRepository;
import com.zhangyuan.modules.product.repository.ProductFeatureRepository;
import com.zhangyuan.modules.product.repository.ProductPlanGroupRepository;
import com.zhangyuan.modules.product.repository.ProductPlanRepository;
import com.zhangyuan.modules.product.repository.ProductPriceRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
    "spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
    "spring.cloud.nacos.config.import-check.enabled=false",
    "zhangyuan.minio.endpoint=http://localhost:9000",
    "zhangyuan.minio.access-key=test",
    "zhangyuan.minio.secret-key=test",
    "zhangyuan.minio.bucket=test"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class OperationLogAspectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock all JPA repositories + external services
    @MockBean private AssetFileJpaRepository assetFileRepository;
    @MockBean private CmsPageRepository cmsPageRepository;
    @MockBean private CmsPageTranslationRepository cmsPageTranslationRepository;
    @MockBean private CmsPageVersionRepository cmsPageVersionRepository;
    @MockBean private CmsPublishRecordRepository cmsPublishRecordRepository;
    @MockBean private CmsBlockDefinitionRepository cmsBlockDefinitionRepository;
    @MockBean private ProductPlanGroupRepository productPlanGroupRepository;
    @MockBean private ProductPlanRepository productPlanRepository;
    @MockBean private ProductPriceRepository productPriceRepository;
    @MockBean private ProductFeatureRepository productFeatureRepository;
    @MockBean private OrderMainEntityRepository orderMainRepository;
    @MockBean private PaymentTransactionJpaRepository paymentTransactionJpaRepository;
    @MockBean private MinioClient minioClient;
    @MockBean private OperationLogJpaRepository operationLogJpaRepository;

    @Test
    void operationLogAnnotationTriggersOnCreateCmsPage() throws Exception {
        // Mock CmsPage persistence chain
        var cmsPageEntity = new com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage(
                "/log-test", "zh-CN", "custom", 1L);
        var idField = com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage.class
                .getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(cmsPageEntity, 100L);
        when(cmsPageRepository.save(any())).thenReturn(cmsPageEntity);
        when(cmsPageRepository.existsBySlug(any())).thenReturn(false);

        // Mock CmsPageTranslation persistence chain
        var translationEntity = new com.zhangyuan.modules.cms.adapter.out.persistence.CmsPageTranslation(
                100L, "zh-CN", "Test");
        when(cmsPageTranslationRepository.save(any())).thenReturn(translationEntity);

        // Mock operation log repository save to return a real entity
        when(operationLogJpaRepository.save(any())).thenAnswer(invocation -> {
            OperationLogEntity entity = invocation.getArgument(0);
            var logIdField = OperationLogEntity.class.getDeclaredField("id");
            logIdField.setAccessible(true);
            logIdField.set(entity, 1L);
            return entity;
        });

        // Hit the DDD endpoint (no auth required for /api/**)
        var result = mockMvc.perform(post("/api/ddd/cms/pages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"slug": "/log-test", "title": "Test", "defaultLocale": "zh-CN", "pageType": "custom"}
                                """))
                .andReturn();

        assertThat(result.getResponse().getStatus())
                .as("Expected 200, got: " + result.getResponse().getContentAsString())
                .isEqualTo(200);

        verify(operationLogJpaRepository, atLeastOnce()).save(any(OperationLogEntity.class));
    }
}

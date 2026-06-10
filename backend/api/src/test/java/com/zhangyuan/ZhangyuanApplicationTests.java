package com.zhangyuan;

import com.zhangyuan.modules.asset.adapter.out.persistence.AssetFileJpaRepository;
import com.zhangyuan.common.accesslog.adapter.out.persistence.AccessLogJpaRepository;
import com.zhangyuan.common.operationlog.adapter.out.persistence.OperationLogJpaRepository;
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
import com.zhangyuan.modules.ai.config.ModelPricingRepository;
import com.zhangyuan.modules.ai.config.ModelRouteRepository;
import com.zhangyuan.modules.ai.config.PlanModelAccessRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

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
class ZhangyuanApplicationTests {

    @MockBean
    private AssetFileJpaRepository assetFileRepository;

    @MockBean
    private CmsPageRepository cmsPageRepository;

    @MockBean
    private CmsPageTranslationRepository cmsPageTranslationRepository;

    @MockBean
    private CmsPageVersionRepository cmsPageVersionRepository;

    @MockBean
    private CmsPublishRecordRepository cmsPublishRecordRepository;

    @MockBean
    private CmsBlockDefinitionRepository cmsBlockDefinitionRepository;

    @MockBean
    private ProductPlanGroupRepository productPlanGroupRepository;

    @MockBean
    private ProductPlanRepository productPlanRepository;

    @MockBean
    private ProductPriceRepository productPriceRepository;

    @MockBean
    private ProductFeatureRepository productFeatureRepository;

    @MockBean
    private OrderMainEntityRepository orderMainRepository;

    @MockBean
    private PaymentTransactionJpaRepository paymentTransactionJpaRepository;

    @MockBean
    private OperationLogJpaRepository operationLogJpaRepository;

    @MockBean
    private AccessLogJpaRepository accessLogJpaRepository;

    @MockBean
    private MinioClient minioClient;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ModelPricingRepository modelPricingRepository;

    @MockBean
    private ModelRouteRepository modelRouteRepository;

    @MockBean
    private PlanModelAccessRepository planModelAccessRepository;

    @Test
    void contextLoads() {
    }
}

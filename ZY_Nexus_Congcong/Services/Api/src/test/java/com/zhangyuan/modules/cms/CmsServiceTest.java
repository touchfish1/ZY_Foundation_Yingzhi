package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.domain.CmsPage;
import com.zhangyuan.modules.cms.domain.CmsPageTranslation;
import com.zhangyuan.modules.cms.domain.CmsPageVersion;
import com.zhangyuan.modules.cms.dto.PublishPageRequest;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.product.ProductService;
import com.zhangyuan.modules.product.dto.FeatureResponse;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import com.zhangyuan.modules.product.dto.PlanResponse;
import com.zhangyuan.modules.product.dto.PriceResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CmsServiceTest {

    private final CmsPageRepository pageRepository = mock(CmsPageRepository.class);
    private final CmsPageTranslationRepository translationRepository = mock(CmsPageTranslationRepository.class);
    private final CmsPageVersionRepository versionRepository = mock(CmsPageVersionRepository.class);
    private final CmsPublishRecordRepository publishRecordRepository = mock(CmsPublishRecordRepository.class);
    private final ProductService productService = mock(ProductService.class);
    private final CmsService cmsService = new CmsService(pageRepository, translationRepository, versionRepository, publishRecordRepository, productService);

    @Test
    void publishInjectsPricingPlanGroupSnapshot() {
        CmsPageVersion version = preparePublishVersion(Map.of(
                "layout", "default",
                "blocks", List.of(pricingBlock("vip", "yearly"))
        ));
        when(productService.findGroupByCode("vip")).thenReturn(Optional.of(new PlanGroupResponse(
                1L,
                "vip",
                "VIP",
                "VIP plans",
                "enabled",
                10,
                List.of(new PlanResponse(
                        11L,
                        "pro",
                        "Pro",
                        "Pro plan",
                        "Hot",
                        "enabled",
                        1,
                        List.of(new PriceResponse(21L, "CNY", "yearly", BigDecimal.valueOf(199), BigDecimal.valueOf(299), "enabled")),
                        List.of(new FeatureResponse(31L, "storage", "100GB", true, 1))
                ))
        )));

        cmsService.publish(100L, "zh-CN", new PublishPageRequest("release"));

        Map<String, Object> props = pricingProps(version);
        assertThat(props).containsEntry("planGroupCode", "vip").containsEntry("defaultBillingCycle", "yearly").containsEntry("dataStatus", "ready");
        assertThat(props.get("planGroup")).isInstanceOf(Map.class);
        assertThat((List<?>) props.get("plans")).hasSize(1);
        Map<String, Object> plan = (Map<String, Object>) ((List<?>) props.get("plans")).getFirst();
        assertThat(plan.get("code")).isEqualTo("pro");
        assertThat((List<?>) plan.get("prices")).hasSize(1);
        assertThat((List<?>) plan.get("features")).hasSize(1);
        verify(publishRecordRepository).save(any());
    }

    @Test
    void publishKeepsGoingWhenPricingPlanGroupIsMissing() {
        CmsPageVersion version = preparePublishVersion(Map.of("blocks", List.of(pricingBlock("missing", "monthly"))));
        when(productService.findGroupByCode("missing")).thenReturn(Optional.empty());

        cmsService.publish(100L, "zh-CN", null);

        Map<String, Object> props = pricingProps(version);
        assertThat(props).containsEntry("planGroupCode", "missing").containsEntry("defaultBillingCycle", "monthly");
        assertThat(props).containsEntry("dataStatus", "missing").containsEntry("dataError", "Product plan group not found");
        assertThat((List<?>) props.get("plans")).isEmpty();
        verify(publishRecordRepository).save(any());
    }

    private CmsPageVersion preparePublishVersion(Map<String, Object> content) {
        CmsPage page = new CmsPage("/pricing", "zh-CN", null);
        CmsPageTranslation translation = new CmsPageTranslation(100L, "zh-CN", "Pricing");
        translation.updateDraft("Pricing", null, null, null, 200L);
        CmsPageVersion version = new CmsPageVersion(100L, "zh-CN", 1, content, null, null);
        when(pageRepository.findById(100L)).thenReturn(Optional.of(page));
        when(translationRepository.findByPageIdAndLocale(100L, "zh-CN")).thenReturn(Optional.of(translation));
        when(versionRepository.findById(200L)).thenReturn(Optional.of(version));
        when(translationRepository.findByPageId(100L)).thenReturn(List.of(translation));
        return version;
    }

    private Map<String, Object> pricingBlock(String planGroupCode, String defaultBillingCycle) {
        return Map.of(
                "type", "pricing",
                "props", Map.of(
                        "planGroupCode", planGroupCode,
                        "defaultBillingCycle", defaultBillingCycle
                )
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pricingProps(CmsPageVersion version) {
        List<?> blocks = (List<?>) version.getSnapshotJson().get("blocks");
        return (Map<String, Object>) ((Map<?, ?>) blocks.getFirst()).get("props");
    }
}

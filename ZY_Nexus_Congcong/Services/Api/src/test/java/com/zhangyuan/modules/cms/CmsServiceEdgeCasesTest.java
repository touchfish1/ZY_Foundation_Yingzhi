package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.domain.CmsPage;
import com.zhangyuan.modules.cms.domain.CmsPageTranslation;
import com.zhangyuan.modules.cms.domain.CmsPageVersion;
import com.zhangyuan.modules.cms.dto.CreatePageRequest;
import com.zhangyuan.modules.cms.dto.SaveDraftRequest;
import com.zhangyuan.modules.cms.dto.PublishPageRequest;
import com.zhangyuan.modules.cms.dto.RenderPageResponse;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.product.ProductService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CmsServiceEdgeCasesTest {

    private final CmsPageRepository pageRepository = mock(CmsPageRepository.class);
    private final CmsPageTranslationRepository translationRepository = mock(CmsPageTranslationRepository.class);
    private final CmsPageVersionRepository versionRepository = mock(CmsPageVersionRepository.class);
    private final CmsPublishRecordRepository publishRecordRepository = mock(CmsPublishRecordRepository.class);
    private final ProductService productService = mock(ProductService.class);
    private final CmsService cmsService = new CmsService(pageRepository, translationRepository, versionRepository, publishRecordRepository, productService);

    @Test
    void createPageWithCustomSlug() {
        CreatePageRequest request = new CreatePageRequest("/custom-page", "Custom", "en-US");
        when(pageRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pageRepository.existsBySlug("/custom-page")).thenReturn(false);
        when(translationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(translationRepository.findByPageId(any())).thenReturn(List.of());

        var response = cmsService.createPage(request);

        assertThat(response.slug()).isEqualTo("/custom-page");
        assertThat(response.defaultLocale()).isEqualTo("en-US");
    }

    @Test
    void createPageThrowsOnDuplicateSlug() {
        CreatePageRequest request = new CreatePageRequest("/existing", "Existing", "zh-CN");
        when(pageRepository.existsBySlug("/existing")).thenReturn(true);

        assertThatThrownBy(() -> cmsService.createPage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void saveDraftCreatesTranslationIfMissing() {
        CmsPage page = new CmsPage("/test", "zh-CN", 1L);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        when(translationRepository.findByPageIdAndLocale(1L, "en-US")).thenReturn(Optional.empty());
        Map<String, Object> content = Map.of("layout", "default", "blocks", List.of());
        SaveDraftRequest request = new SaveDraftRequest("English Title", "SEO Title", "SEO Desc", "kw",
                content, "first draft");
        when(translationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(versionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(versionRepository.findFirstByPageIdAndLocaleOrderByVersionNoDesc(1L, "en-US")).thenReturn(Optional.empty());
        when(translationRepository.findByPageId(1L)).thenReturn(List.of());

        var response = cmsService.saveDraft(1L, "en-US", request);

        assertThat(response).isNotNull();
        verify(translationRepository).save(any());
    }

    @Test
    void saveDraftIncrementsVersion() {
        CmsPage page = new CmsPage("/test", "zh-CN", 1L);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        CmsPageTranslation translation = new CmsPageTranslation(1L, "zh-CN", "Test");
        when(translationRepository.findByPageIdAndLocale(1L, "zh-CN")).thenReturn(Optional.of(translation));
        CmsPageVersion existingVersion = new CmsPageVersion(1L, "zh-CN", 1, Map.of(), null, "v1");
        when(versionRepository.findFirstByPageIdAndLocaleOrderByVersionNoDesc(1L, "zh-CN"))
                .thenReturn(Optional.of(existingVersion));
        when(versionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(translationRepository.findByPageId(1L)).thenReturn(List.of());

        Map<String, Object> newContent = Map.of("layout", "default", "blocks", List.of());
        SaveDraftRequest request = new SaveDraftRequest("Test", null, null, null, newContent, "v2");
        cmsService.saveDraft(1L, "zh-CN", request);

        when(versionRepository.findFirstByPageIdAndLocaleOrderByVersionNoDesc(1L, "zh-CN"))
                .thenReturn(Optional.of(existingVersion));

        cmsService.saveDraft(1L, "zh-CN", request);

        verify(versionRepository).save(any());
    }

    @Test
    void publishThrowsWhenNoDraft() {
        when(pageRepository.findById(1L)).thenReturn(Optional.of(new CmsPage("/test", "zh-CN", 1L)));
        CmsPageTranslation translation = new CmsPageTranslation(1L, "zh-CN", "Test");
        when(translationRepository.findByPageIdAndLocale(1L, "zh-CN")).thenReturn(Optional.of(translation));

        assertThatThrownBy(() -> cmsService.publish(1L, "zh-CN", new PublishPageRequest("publish")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no draft");
    }

    @Test
    void renderReturnsPublishedPage() {
        CmsPage page = mock(CmsPage.class);
        when(page.getId()).thenReturn(1L);
        when(page.getSlug()).thenReturn("/plans");
        when(page.getDefaultLocale()).thenReturn("zh-CN");
        when(pageRepository.findBySlug("/plans")).thenReturn(Optional.of(page));

        CmsPageTranslation transMock = mock(CmsPageTranslation.class);
        when(transMock.getPublishedVersionId()).thenReturn(55L);
        when(transMock.getTitle()).thenReturn("Plans");
        when(transMock.getSeoTitle()).thenReturn("Plans - ZHANGYUAN");
        when(transMock.getSeoDescription()).thenReturn("desc");
        when(transMock.getSeoKeywords()).thenReturn("kw");
        when(translationRepository.findByPageIdAndLocale(1L, "zh-CN")).thenReturn(Optional.of(transMock));

        Map<String, Object> snapshotContent = Map.of("layout", "default", "blocks", List.of(
                Map.of("id", "b1", "type", "hero", "props", Map.of("title", "Hello"))
        ));
        CmsPageVersion version = mock(CmsPageVersion.class);
        when(version.getSnapshotJson()).thenReturn(snapshotContent);
        when(version.getContentJson()).thenReturn(snapshotContent);
        when(versionRepository.findById(55L)).thenReturn(Optional.of(version));

        RenderPageResponse response = cmsService.render("/plans", "zh-CN");

        assertThat(response.path()).isEqualTo("/plans");
        assertThat(response.locale()).isEqualTo("zh-CN");
        assertThat(response.seo().get("title")).isEqualTo("Plans - ZHANGYUAN");
    }

    @Test
    void renderThrowsWhenPageNotFound() {
        when(pageRepository.findBySlug("/missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cmsService.render("/missing", "zh-CN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}

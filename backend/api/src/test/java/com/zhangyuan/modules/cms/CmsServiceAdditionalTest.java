package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage;
import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPageTranslation;
import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPageVersion;
import com.zhangyuan.modules.cms.application.service.CmsApplicationService;
import com.zhangyuan.modules.cms.dto.PublishPageRequest;
import com.zhangyuan.modules.cms.dto.UpdatePageRequest;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
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

class CmsServiceAdditionalTest {

    private final CmsPageRepository pageRepository = mock(CmsPageRepository.class);
    private final CmsPageTranslationRepository translationRepository = mock(CmsPageTranslationRepository.class);
    private final CmsPageVersionRepository versionRepository = mock(CmsPageVersionRepository.class);
    private final CmsPublishRecordRepository publishRecordRepository = mock(CmsPublishRecordRepository.class);
    private final CmsBlockDefinitionRepository blockDefinitionRepository = mock(CmsBlockDefinitionRepository.class);
    private final ProductApplicationService productService = mock(ProductApplicationService.class);
    private final CmsApplicationService cmsService = new CmsApplicationService(pageRepository, translationRepository, versionRepository, publishRecordRepository, blockDefinitionRepository, productService);

    @Test
    void previewReturnsSpecificVersion() {
        CmsPage page = new CmsPage("/test", "zh-CN", null);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        Map<String, Object> content = Map.of("blocks", List.of());
        CmsPageVersion version = new CmsPageVersion(1L, "zh-CN", 1, content, null, null);
        when(versionRepository.findById(99L)).thenReturn(Optional.of(version));

        Map<String, Object> result = cmsService.preview(1L, "zh-CN", 99L);

        assertThat(result).isEqualTo(content);
    }

    @Test
    void previewWithoutVersionIdReturnsLatestDraft() {
        CmsPage page = new CmsPage("/test", "zh-CN", null);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        CmsPageTranslation translation = new CmsPageTranslation(1L, "zh-CN", "Test");
        translation.updateDraft("Test", null, null, null, 55L);
        when(translationRepository.findByPageIdAndLocale(1L, "zh-CN")).thenReturn(Optional.of(translation));
        Map<String, Object> content = Map.of("blocks", List.of());
        CmsPageVersion version = new CmsPageVersion(1L, "zh-CN", 1, content, null, null);
        when(versionRepository.findById(55L)).thenReturn(Optional.of(version));

        Map<String, Object> result = cmsService.preview(1L, "zh-CN", null);

        assertThat(result).isEqualTo(content);
    }

    @Test
    void previewThrowsWhenNoDraftVersion() {
        CmsPage page = new CmsPage("/test", "zh-CN", null);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        CmsPageTranslation translation = new CmsPageTranslation(1L, "zh-CN", "Test");
        when(translationRepository.findByPageIdAndLocale(1L, "zh-CN")).thenReturn(Optional.of(translation));

        assertThatThrownBy(() -> cmsService.preview(1L, "zh-CN", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updatePageChangesSlug() {
        CmsPage page = new CmsPage("/old-slug", "zh-CN", null);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        when(pageRepository.existsBySlug("/new-slug")).thenReturn(false);
        when(translationRepository.findByPageId(1L)).thenReturn(List.of(new CmsPageTranslation(1L, "zh-CN", "Test")));

        var response = cmsService.updatePage(1L, new UpdatePageRequest("/new-slug", null));

        assertThat(response.slug()).isEqualTo("/new-slug");
        verify(pageRepository).findById(1L);
    }

    @Test
    void updatePageThrowsOnDuplicateSlug() {
        CmsPage page = new CmsPage("/old-slug", "zh-CN", null);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        when(pageRepository.existsBySlug("/existing")).thenReturn(true);

        assertThatThrownBy(() -> cmsService.updatePage(1L, new UpdatePageRequest("/existing", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deletePageRemovesAllRelatedData() {
        CmsPage page = new CmsPage("/test", "zh-CN", null);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        CmsPageTranslation translation = new CmsPageTranslation(1L, "zh-CN", "Test");
        when(translationRepository.findByPageId(1L)).thenReturn(List.of(translation));

        cmsService.deletePage(1L);

        verify(versionRepository).deleteByPageIdAndLocale(1L, "zh-CN");
        verify(publishRecordRepository).deleteByPageIdAndLocale(1L, "zh-CN");
        verify(translationRepository).deleteByPageId(1L);
        verify(pageRepository).delete(page);
    }

    @Test
    void rollbackCreatesNewVersionFromSource() {
        CmsPage page = new CmsPage("/test", "zh-CN", null);
        when(pageRepository.findById(1L)).thenReturn(Optional.of(page));
        CmsPageTranslation translation = new CmsPageTranslation(1L, "zh-CN", "Test");
        translation.updateDraft("Test", null, null, null, 55L);
        when(translationRepository.findByPageIdAndLocale(1L, "zh-CN")).thenReturn(Optional.of(translation));

        Map<String, Object> sourceContent = Map.of("blocks", List.of(Map.of("type", "hero", "props", Map.of())));
        CmsPageVersion sourceVersion = new CmsPageVersion(1L, "zh-CN", 1, sourceContent, null, "original");
        when(versionRepository.findById(33L)).thenReturn(Optional.of(sourceVersion));
        when(versionRepository.findFirstByPageIdAndLocaleOrderByVersionNoDesc(1L, "zh-CN"))
                .thenReturn(Optional.of(sourceVersion));
        when(translationRepository.findByPageId(1L)).thenReturn(List.of(translation));

        var response = cmsService.rollback(1L, "zh-CN", new PublishPageRequest(33L, "rollback to v1"));

        assertThat(response).isNotNull();
        verify(versionRepository).save(any(CmsPageVersion.class));
    }
}

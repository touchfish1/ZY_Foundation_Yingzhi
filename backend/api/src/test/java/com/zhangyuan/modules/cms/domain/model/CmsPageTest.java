package com.zhangyuan.modules.cms.domain.model;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CmsPageTest {

    @Test
    void createPage() {
        CmsPage page = new CmsPage("/plans", "zh-CN", 1L);
        assertThat(page.getSlug()).isEqualTo("/plans");
        assertThat(page.getDefaultLocale()).isEqualTo("zh-CN");
        assertThat(page.isEnabled()).isTrue();
    }

    @Test
    void addTranslation() {
        CmsPage page = new CmsPage("/plans", "zh-CN", 1L);
        page.addTranslation("en-US", "Plans");
        assertThat(page.getTranslations()).hasSize(1);
        assertThat(page.getTranslation("en-US").getTitle()).isEqualTo("Plans");
    }

    @Test
    void changeSlug() {
        CmsPage page = new CmsPage("/old", "zh-CN", 1L);
        page.changeSlug("/new");
        assertThat(page.getSlug()).isEqualTo("/new");
    }

    @Test
    void disableAndEnable() {
        CmsPage page = new CmsPage("/test", "zh-CN", 1L);
        page.disable();
        assertThat(page.isEnabled()).isFalse();
        page.enable();
        assertThat(page.isEnabled()).isTrue();
    }

    @Test
    void normalizeSlug() {
        assertThat(CmsPage.normalizeSlug("test")).isEqualTo("/test");
        assertThat(CmsPage.normalizeSlug("/test/")).isEqualTo("/test");
    }

    @Test
    void blankSlugThrows() {
        assertThatThrownBy(() -> CmsPage.normalizeSlug("")).isInstanceOf(IllegalArgumentException.class);
    }
}

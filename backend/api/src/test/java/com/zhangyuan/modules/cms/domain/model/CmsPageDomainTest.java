package com.zhangyuan.modules.cms.domain.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class CmsPageDomainTest {

    @Test
    void create() {
        CmsPage p = new CmsPage("/plans", "zh-CN", "custom", 1L);
        assertThat(p.getSlug()).isEqualTo("/plans");
        assertThat(p.isEnabled()).isTrue();
    }

    @Test
    void addTranslation() {
        CmsPage p = new CmsPage("/plans", "zh-CN", "custom", 1L);
        p.addTranslation("en-US", "Plans");
        assertThat(p.getTranslations()).hasSize(1);
        assertThat(p.getTranslation("en-US").getTitle()).isEqualTo("Plans");
    }

    @Test
    void changeSlug() {
        CmsPage p = new CmsPage("/old", "zh-CN", "custom", 1L);
        p.changeSlug("/new");
        assertThat(p.getSlug()).isEqualTo("/new");
    }

    @Test
    void saveDraftAndPublish() {
        CmsPage p = new CmsPage("/test", "zh-CN", "custom", 1L);
        PageTranslation t = p.addTranslation("zh-CN", "Test");
        t.saveDraft(Map.of("blocks", List.of()), "v1");
        t.updateDraftInfo("Test", null, null, null, 1L);
        assertThat(t.getDraftVersionId()).isEqualTo(1L);
        t.publish(1L);
        assertThat(t.getPublishedVersionId()).isEqualTo(1L);
        assertThat(t.getStatus()).isEqualTo("published");
    }
}

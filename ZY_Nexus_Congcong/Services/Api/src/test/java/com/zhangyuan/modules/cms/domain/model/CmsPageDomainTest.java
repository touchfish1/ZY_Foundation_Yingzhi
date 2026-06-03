package com.zhangyuan.modules.cms.domain.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class CmsPageDomainTest {

    @Test
    void create() {
        CmsPage p = new CmsPage("/plans", "zh-CN", 1L);
        assertThat(p.getSlug()).isEqualTo("/plans");
        assertThat(p.isEnabled()).isTrue();
    }

    @Test
    void addTranslation() {
        CmsPage p = new CmsPage("/plans", "zh-CN", 1L);
        p.addTranslation("en-US", "Plans");
        assertThat(p.getTranslations()).hasSize(1);
        assertThat(p.getTranslation("en-US").getTitle()).isEqualTo("Plans");
    }

    @Test
    void changeSlug() {
        CmsPage p = new CmsPage("/old", "zh-CN", 1L);
        p.changeSlug("/new");
        assertThat(p.getSlug()).isEqualTo("/new");
    }

    @Test
    void saveDraftAndPublish() {
        CmsPage p = new CmsPage("/test", "zh-CN", 1L);
        PageTranslation t = p.addTranslation("zh-CN", "Test");
        t.saveDraft(Map.of("blocks", List.of()), "v1");
        assertThat(t.getDraftVersionNo()).isEqualTo(1);
        t.publish("release");
        assertThat(t.getPublishedVersionNo()).isEqualTo(1);
    }
}

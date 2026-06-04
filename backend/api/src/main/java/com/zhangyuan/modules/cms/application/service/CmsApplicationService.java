package com.zhangyuan.modules.cms.application.service;

import com.zhangyuan.modules.cms.domain.model.CmsPage;
import com.zhangyuan.modules.cms.domain.model.PageTranslation;
import com.zhangyuan.modules.cms.domain.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.domain.service.CmsDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CMS 应用服务，负责页面创建、草稿保存和发布的编排。
 */
@Service
public class CmsApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CmsApplicationService.class);

    private final CmsPageRepository cmsPageRepository;
    private final CmsDomainService cmsDomainService;

    public CmsApplicationService(CmsPageRepository cmsPageRepository, CmsDomainService cmsDomainService) {
        this.cmsPageRepository = cmsPageRepository;
        this.cmsDomainService = cmsDomainService;
    }

    /**
     * 创建 CMS 页面。
     *
     * @param slug          页面路径
     * @param defaultLocale 默认语言
     * @param createdBy     创建人 ID
     * @return 创建的页面
     */
    @Transactional
    public CmsPage createPage(String slug, String defaultLocale, Long createdBy) {
        log.info("Creating CMS page: slug={}, locale={}, createdBy={}", slug, defaultLocale, createdBy);
        CmsPage page = cmsDomainService.createPage(cmsPageRepository, slug, defaultLocale, createdBy);
        page.addTranslation(defaultLocale != null ? defaultLocale : "zh-CN", slug);
        CmsPage saved = cmsPageRepository.save(page);
        log.info("CMS page created: id={}, slug={}", saved.getId(), saved.getSlug());
        return saved;
    }

    /**
     * 根据 ID 查询页面。
     *
     * @param id 页面 ID
     * @return 页面 Optional
     */
    @Transactional(readOnly = true)
    public Optional<CmsPage> findById(Long id) {
        return cmsPageRepository.findById(id);
    }

    /**
     * 根据 slug 查询页面。
     *
     * @param slug 页面路径
     * @return 页面 Optional
     */
    @Transactional(readOnly = true)
    public Optional<CmsPage> findBySlug(String slug) {
        return cmsPageRepository.findBySlug(slug);
    }

    /**
     * 获取所有页面列表。
     *
     * @return 页面列表
     */
    @Transactional(readOnly = true)
    public List<CmsPage> listAll() {
        return cmsPageRepository.findAll();
    }

    /**
     * 删除页面。
     *
     * @param id 页面 ID
     */
    @Transactional
    public void deletePage(Long id) {
        log.info("Deleting CMS page: {}", id);
        cmsPageRepository.deleteById(id);
        log.info("CMS page deleted: {}", id);
    }

    /**
     * 保存草稿版本。
     *
     * @param pageId  页面 ID
     * @param locale  语言环境
     * @param title   标题
     * @param content 内容
     * @param remark  备注
     * @return 翻译对象
     */
    @Transactional
    public PageTranslation saveDraft(Long pageId, String locale, String title, Map<String, Object> content, String remark) {
        log.info("Saving draft for CMS page: {}, locale: {}", pageId, locale);
        CmsPage page = cmsPageRepository.findById(pageId)
                .orElseThrow(() -> {
                    log.error("CMS page not found: {}", pageId);
                    return new IllegalArgumentException("CMS page not found: " + pageId);
                });
        // 获取或创建翻译
        PageTranslation translation;
        try {
            translation = page.getTranslation(locale);
        } catch (IllegalArgumentException e) {
            log.warn("Translation not found for locale {}, creating new one", locale);
            translation = page.addTranslation(locale, title);
        }
        translation.saveDraft(content, remark);
        cmsPageRepository.save(page);
        log.info("Draft saved for CMS page: {}, locale: {}", pageId, locale);
        return translation;
    }

    /**
     * 发布草稿。
     *
     * @param pageId 页面 ID
     * @param locale 语言环境
     * @param remark 发布备注
     * @return 翻译对象
     */
    @Transactional
    public PageTranslation publish(Long pageId, String locale, String remark) {
        log.info("Publishing CMS page: {}, locale: {}", pageId, locale);
        CmsPage page = cmsPageRepository.findById(pageId)
                .orElseThrow(() -> {
                    log.error("CMS page not found: {}", pageId);
                    return new IllegalArgumentException("CMS page not found: " + pageId);
                });
        PageTranslation translation = page.getTranslation(locale);
        translation.publish(remark);
        cmsPageRepository.save(page);
        log.info("CMS page published: {}, locale: {}", pageId, locale);
        return translation;
    }
}

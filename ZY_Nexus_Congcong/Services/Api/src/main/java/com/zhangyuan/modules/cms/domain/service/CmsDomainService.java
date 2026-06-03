package com.zhangyuan.modules.cms.domain.service;

import com.zhangyuan.modules.cms.domain.model.CmsPage;
import com.zhangyuan.modules.cms.domain.repository.CmsPageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * CMS 领域服务，包含页面创建的核心业务规则。
 */
@Service
public class CmsDomainService {

    private static final Logger log = LoggerFactory.getLogger(CmsDomainService.class);

    /**
     * 创建 CMS 页面领域对象，验证 slug 唯一性。
     *
     * @param repository    CMS 页面仓库
     * @param slug          页面路径
     * @param defaultLocale 默认语言
     * @param createdBy     创建人 ID
     * @return CMS 页面领域对象
     * @throws IllegalArgumentException slug 已存在时抛出
     */
    public CmsPage createPage(CmsPageRepository repository, String slug, String defaultLocale, Long createdBy) {
        String normalizedSlug = CmsPage.normalizeSlug(slug);
        if (repository.existsBySlug(normalizedSlug)) {
            log.warn("CMS page slug already exists: {}", normalizedSlug);
            throw new IllegalArgumentException("CMS page slug already exists: " + normalizedSlug);
        }
        return new CmsPage(normalizedSlug, defaultLocale, createdBy);
    }
}

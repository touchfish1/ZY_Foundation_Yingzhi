package com.zhangyuan.modules.cms.adapter.out.persistence;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.cms.domain.model.CmsPage;
import com.zhangyuan.modules.cms.domain.repository.CmsPageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaCmsPageRepository implements CmsPageRepository {

    private final com.zhangyuan.modules.cms.repository.CmsPageRepository jpaRepository;

    public JpaCmsPageRepository(com.zhangyuan.modules.cms.repository.CmsPageRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CmsPage> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<CmsPage> findBySlug(String slug) {
        return jpaRepository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public List<CmsPage> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public PageResponse<CmsPage> findAll(int page, int pageSize) {
        var springPage = jpaRepository.findAll(PageRequest.of(page - 1, pageSize));
        var items = springPage.getContent().stream().map(this::toDomain).toList();
        return PageResponse.of(items, page, pageSize, springPage.getTotalElements());
    }

    @Override
    public CmsPage save(CmsPage page) {
        com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage entity;
        if (page.getId() != null) {
            entity = jpaRepository.findById(page.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Page not found: " + page.getId()));
            entity.setSlug(page.getSlug());
            entity.setDefaultLocale(page.getDefaultLocale());
            entity.setPageType(page.getPageType());
            if (page.isEnabled()) {
                entity.enable();
            } else {
                entity.disable();
            }
            entity.touch();
        } else {
            entity = new com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage(
                    page.getSlug(), page.getDefaultLocale(), page.getPageType(), page.getCreatedBy());
            if (page.isEnabled()) {
                entity.enable();
            } else {
                entity.disable();
            }
        }
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public List<CmsPage> findByPageTypeAndStatus(String pageType, String status) {
        return jpaRepository.findByPageTypeAndStatus(pageType, status, PageRequest.of(0, Integer.MAX_VALUE))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public PageResponse<CmsPage> findByPageTypeAndStatus(String pageType, String status, int page, int pageSize) {
        var springPage = jpaRepository.findByPageTypeAndStatus(pageType, status, PageRequest.of(page - 1, pageSize));
        var items = springPage.getContent().stream().map(this::toDomain).toList();
        return PageResponse.of(items, page, pageSize, springPage.getTotalElements());
    }

    @Override
    public PageResponse<CmsPage> findByKeyword(String keyword, int page, int pageSize) {
        var springPage = jpaRepository.findByKeyword(keyword, PageRequest.of(page - 1, pageSize));
        var items = springPage.getContent().stream().map(this::toDomain).toList();
        return PageResponse.of(items, page, pageSize, springPage.getTotalElements());
    }

    private CmsPage toDomain(com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage entity) {
        CmsPage page = new CmsPage(entity.getSlug(), entity.getDefaultLocale(), entity.getPageType(), entity.getCreatedBy());
        page.setId(entity.getId());
        if (CmsPage.STATUS_ENABLED.equals(entity.getStatus())) {
            page.enable();
        } else {
            page.disable();
        }
        return page;
    }
}

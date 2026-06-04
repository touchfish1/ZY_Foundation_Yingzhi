package com.zhangyuan.modules.cms.adapter.out.persistence;

import com.zhangyuan.modules.cms.domain.model.CmsPage;
import com.zhangyuan.modules.cms.domain.repository.CmsPageRepository;
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
    public CmsPage save(CmsPage page) {
        com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage entity;
        if (page.getId() != null) {
            entity = jpaRepository.findById(page.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Page not found: " + page.getId()));
            entity.setSlug(page.getSlug());
            entity.setDefaultLocale(page.getDefaultLocale());
            entity.touch();
        } else {
            entity = new com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage(
                    page.getSlug(), page.getDefaultLocale(), page.getCreatedBy());
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

    private CmsPage toDomain(com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage entity) {
        CmsPage page = new CmsPage(entity.getSlug(), entity.getDefaultLocale(), null);
        return page;
    }
}

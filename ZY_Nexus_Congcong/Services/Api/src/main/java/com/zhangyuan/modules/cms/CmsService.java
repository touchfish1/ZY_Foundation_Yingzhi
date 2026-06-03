package com.zhangyuan.modules.cms;

import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.modules.cms.domain.CmsPage;
import com.zhangyuan.modules.cms.domain.CmsPageTranslation;
import com.zhangyuan.modules.cms.domain.CmsPageVersion;
import com.zhangyuan.modules.cms.domain.CmsPublishRecord;
import com.zhangyuan.modules.cms.dto.CreatePageRequest;
import com.zhangyuan.modules.cms.dto.PageDetailResponse;
import com.zhangyuan.modules.cms.dto.PageListItemResponse;
import com.zhangyuan.modules.cms.dto.PageTranslationResponse;
import com.zhangyuan.modules.cms.dto.PublishPageRequest;
import com.zhangyuan.modules.cms.dto.RenderPageResponse;
import com.zhangyuan.modules.cms.dto.SaveDraftRequest;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CmsService {

    private final CmsPageRepository pageRepository;
    private final CmsPageTranslationRepository translationRepository;
    private final CmsPageVersionRepository versionRepository;
    private final CmsPublishRecordRepository publishRecordRepository;

    public CmsService(CmsPageRepository pageRepository, CmsPageTranslationRepository translationRepository,
                      CmsPageVersionRepository versionRepository, CmsPublishRecordRepository publishRecordRepository) {
        this.pageRepository = pageRepository;
        this.translationRepository = translationRepository;
        this.versionRepository = versionRepository;
        this.publishRecordRepository = publishRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<PageListItemResponse> listPages() {
        return pageRepository.findAll().stream()
                .map(page -> new PageListItemResponse(page.getId(), page.getSlug(), page.getDefaultLocale(), page.getStatus(), page.getUpdatedAt()))
                .toList();
    }

    @Transactional
    public PageDetailResponse createPage(CreatePageRequest request) {
        String slug = CmsPage.normalizeSlug(request.slug());
        if (pageRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("CMS page slug already exists");
        }

        String locale = request.defaultLocale() == null || request.defaultLocale().isBlank() ? "zh-CN" : request.defaultLocale();
        CmsPage page = pageRepository.save(new CmsPage(slug, locale, currentUserId()));
        CmsPageTranslation translation = translationRepository.save(new CmsPageTranslation(page.getId(), locale, request.title()));
        return toDetail(page, List.of(translation));
    }

    @Transactional(readOnly = true)
    public PageDetailResponse getPage(Long pageId) {
        CmsPage page = requirePage(pageId);
        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    @Transactional
    public PageDetailResponse saveDraft(Long pageId, String locale, SaveDraftRequest request) {
        CmsPage page = requirePage(pageId);
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseGet(() -> translationRepository.save(new CmsPageTranslation(pageId, locale, request.title())));

        int nextVersion = versionRepository.findFirstByPageIdAndLocaleOrderByVersionNoDesc(pageId, locale)
                .map(version -> version.getVersionNo() + 1)
                .orElse(1);
        CmsPageVersion version = versionRepository.save(new CmsPageVersion(pageId, locale, nextVersion, request.content(), currentUserId(), request.remark()));
        translation.updateDraft(request.title(), request.seoTitle(), request.seoDescription(), request.seoKeywords(), version.getId());
        page.touch();

        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    @Transactional
    public PageDetailResponse publish(Long pageId, String locale, PublishPageRequest request) {
        CmsPage page = requirePage(pageId);
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (translation.getDraftVersionId() == null) {
            throw new IllegalArgumentException("CMS page has no draft to publish");
        }

        CmsPageVersion version = versionRepository.findById(translation.getDraftVersionId())
                .orElseThrow(() -> new IllegalArgumentException("CMS draft version not found"));
        version.publishSnapshot(version.getContentJson());
        translation.publish(version.getId());
        publishRecordRepository.save(new CmsPublishRecord(pageId, locale, version.getId(), currentUserId(), request == null ? null : request.remark()));
        page.touch();

        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    @Transactional(readOnly = true)
    public RenderPageResponse render(String path, String locale) {
        CmsPage page = pageRepository.findBySlug(CmsPage.normalizeSlug(path))
                .orElseThrow(() -> new IllegalArgumentException("CMS page not found"));
        String resolvedLocale = locale == null || locale.isBlank() ? page.getDefaultLocale() : locale;
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(page.getId(), resolvedLocale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (translation.getPublishedVersionId() == null) {
            throw new IllegalArgumentException("CMS page is not published");
        }

        CmsPageVersion version = versionRepository.findById(translation.getPublishedVersionId())
                .orElseThrow(() -> new IllegalArgumentException("CMS published version not found"));
        Map<String, Object> snapshot = version.getSnapshotJson() == null ? version.getContentJson() : version.getSnapshotJson();
        Object layout = snapshot.getOrDefault("layout", "default");

        return new RenderPageResponse(
                page.getSlug(),
                resolvedLocale,
                translation.getTitle(),
                Map.of(
                        "title", translation.getSeoTitle() == null ? translation.getTitle() : translation.getSeoTitle(),
                        "description", translation.getSeoDescription() == null ? "" : translation.getSeoDescription(),
                        "keywords", translation.getSeoKeywords() == null ? "" : translation.getSeoKeywords()
                ),
                String.valueOf(layout),
                snapshot.getOrDefault("blocks", List.of())
        );
    }

    private CmsPage requirePage(Long pageId) {
        return pageRepository.findById(pageId).orElseThrow(() -> new IllegalArgumentException("CMS page not found"));
    }

    private PageDetailResponse toDetail(CmsPage page, List<CmsPageTranslation> translations) {
        return new PageDetailResponse(
                page.getId(),
                page.getSlug(),
                page.getDefaultLocale(),
                page.getStatus(),
                translations.stream().map(this::toTranslation).toList()
        );
    }

    private PageTranslationResponse toTranslation(CmsPageTranslation translation) {
        return new PageTranslationResponse(
                translation.getLocale(),
                translation.getTitle(),
                translation.getSeoTitle(),
                translation.getSeoDescription(),
                translation.getSeoKeywords(),
                translation.getDraftVersionId(),
                translation.getPublishedVersionId(),
                translation.getStatus()
        );
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            return null;
        }
        return user.getId();
    }
}

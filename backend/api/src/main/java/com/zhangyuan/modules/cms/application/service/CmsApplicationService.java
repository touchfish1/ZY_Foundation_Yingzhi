package com.zhangyuan.modules.cms.application.service;

import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.modules.cms.adapter.out.persistence.CmsBlockDefinition;
import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPage;
import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPageTranslation;
import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPageVersion;
import com.zhangyuan.modules.cms.adapter.out.persistence.CmsPublishRecord;
import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import com.zhangyuan.modules.cms.dto.CreatePageRequest;
import com.zhangyuan.modules.cms.dto.PageDetailResponse;
import com.zhangyuan.modules.cms.dto.PageListItemResponse;
import com.zhangyuan.modules.cms.dto.PageTranslationResponse;
import com.zhangyuan.modules.cms.dto.PublishPageRequest;
import com.zhangyuan.modules.cms.dto.RenderPageResponse;
import com.zhangyuan.modules.cms.dto.SaveDraftRequest;
import com.zhangyuan.modules.cms.dto.UpdatePageRequest;
import com.zhangyuan.modules.cms.dto.VersionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;
import com.zhangyuan.modules.product.dto.FeatureResponse;
import com.zhangyuan.modules.product.dto.PlanGroupResponse;
import com.zhangyuan.modules.product.dto.PlanResponse;
import com.zhangyuan.modules.product.dto.PriceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CmsApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CmsApplicationService.class);

    private final CmsPageRepository pageRepository;
    private final CmsPageTranslationRepository translationRepository;
    private final CmsPageVersionRepository versionRepository;
    private final CmsPublishRecordRepository publishRecordRepository;
    private final CmsBlockDefinitionRepository blockDefinitionRepository;
    private final ProductApplicationService productService;

    public CmsApplicationService(CmsPageRepository pageRepository,
                                  CmsPageTranslationRepository translationRepository,
                                  CmsPageVersionRepository versionRepository,
                                  CmsPublishRecordRepository publishRecordRepository,
                                  CmsBlockDefinitionRepository blockDefinitionRepository,
                                  ProductApplicationService productService) {
        this.pageRepository = pageRepository;
        this.translationRepository = translationRepository;
        this.versionRepository = versionRepository;
        this.publishRecordRepository = publishRecordRepository;
        this.blockDefinitionRepository = blockDefinitionRepository;
        this.productService = productService;
    }

    // ── Page CRUD ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PageListItemResponse> listPages() {
        return pageRepository.findAll().stream()
                .map(page -> new PageListItemResponse(page.getId(), page.getSlug(), page.getPageType(), page.getDefaultLocale(), page.getStatus(), page.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<PageListItemResponse> listPublishedByType(String type, int page, int pageSize) {
        String resolvedType = type == null || type.isBlank() ? "custom" : type;
        Page<CmsPage> pageResult = pageRepository.findByPageTypeAndStatus(resolvedType, CmsPage.STATUS_ENABLED, PageRequest.of(page - 1, pageSize));
        List<PageListItemResponse> items = pageResult.getContent().stream()
                .map(p -> new PageListItemResponse(p.getId(), p.getSlug(), p.getPageType(), p.getDefaultLocale(), p.getStatus(), p.getUpdatedAt()))
                .toList();
        return PageResponse.of(items, page, pageSize, pageResult.getTotalElements());
    }

    @Transactional
    public PageDetailResponse createPage(CreatePageRequest request) {
        log.info("Creating CMS page: slug={}, title={}", request.slug(), request.title());
        String slug = CmsPage.normalizeSlug(request.slug());
        if (pageRepository.existsBySlug(slug)) {
            log.warn("CMS page slug already exists: {}", slug);
            throw new IllegalArgumentException("CMS page slug already exists");
        }
        String locale = request.defaultLocale() == null || request.defaultLocale().isBlank() ? "zh-CN" : request.defaultLocale();
        String pageType = request.pageType() == null || request.pageType().isBlank() ? "custom" : request.pageType();
        CmsPage page = pageRepository.save(new CmsPage(slug, locale, pageType, currentUserId()));
        CmsPageTranslation translation = translationRepository.save(new CmsPageTranslation(page.getId(), locale, request.title()));
        log.info("CMS page created: id={}, slug={}", page.getId(), page.getSlug());
        return toDetail(page, List.of(translation));
    }

    @Transactional(readOnly = true)
    public PageDetailResponse getPage(Long pageId) {
        CmsPage page = requirePage(pageId);
        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    @Transactional
    public PageDetailResponse updatePage(Long pageId, UpdatePageRequest request) {
        log.info("Updating CMS page: {}", pageId);
        CmsPage page = requirePage(pageId);
        String slug = CmsPage.normalizeSlug(request.slug());
        if (!slug.equals(page.getSlug()) && pageRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("CMS page slug already exists");
        }
        page.setSlug(slug);
        if (request.defaultLocale() != null && !request.defaultLocale().isBlank()) {
            page.setDefaultLocale(request.defaultLocale());
        }
        if (request.pageType() != null && !request.pageType().isBlank()) {
            page.setPageType(request.pageType());
        }
        page.touch();
        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    @Transactional
    public void deletePage(Long pageId) {
        log.info("Soft-deleting CMS page: {}", pageId);
        CmsPage page = requirePage(pageId);
        page.disable();
        pageRepository.save(page);
        log.info("CMS page soft-deleted: {} (data preserved for published versions)", pageId);
    }

    // ── Draft & Versioning ─────────────────────────────────────

    @Transactional
    public PageDetailResponse saveDraft(Long pageId, String locale, SaveDraftRequest request) {
        log.info("Saving draft for CMS page: {}, locale: {}", pageId, locale);
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

    @Transactional(readOnly = true)
    public Map<String, Object> getDraftVersion(Long pageId, String locale) {
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found for pageId=" + pageId + ", locale=" + locale));
        if (translation.getDraftVersionId() == null) {
            throw new IllegalArgumentException("CMS page translation has no draft version");
        }
        CmsPageVersion version = versionRepository.findById(translation.getDraftVersionId())
                .orElseThrow(() -> new IllegalArgumentException("CMS draft version not found"));
        return version.getContentJson();
    }

    @Transactional(readOnly = true)
    public List<VersionResponse> listVersions(Long pageId, String locale) {
        return versionRepository.findByPageIdAndLocaleOrderByVersionNoDesc(pageId, locale)
                .stream()
                .map(v -> new VersionResponse(v.getId(), v.getVersionNo(), v.getCreatedAt(), v.getRemark()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> preview(Long pageId, String locale, Long versionId) {
        requirePage(pageId);
        if (versionId != null) {
            CmsPageVersion version = versionRepository.findById(versionId)
                    .orElseThrow(() -> new IllegalArgumentException("CMS version not found"));
            return version.getContentJson();
        }
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (translation.getDraftVersionId() == null) {
            throw new IllegalArgumentException("CMS page has no draft version");
        }
        CmsPageVersion version = versionRepository.findById(translation.getDraftVersionId())
                .orElseThrow(() -> new IllegalArgumentException("CMS draft version not found"));
        return version.getContentJson();
    }

    // ── Publish & Rollback ─────────────────────────────────────

    @Transactional
    public PageDetailResponse publish(Long pageId, String locale, PublishPageRequest request) {
        log.info("Publishing CMS page: {}, locale: {}", pageId, locale);
        CmsPage page = requirePage(pageId);
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (translation.getDraftVersionId() == null) {
            throw new IllegalArgumentException("CMS page has no draft to publish");
        }
        CmsPageVersion version = versionRepository.findById(translation.getDraftVersionId())
                .orElseThrow(() -> new IllegalArgumentException("CMS draft version not found"));
        version.publishSnapshot(buildPublishSnapshot(version.getContentJson()));
        translation.publish(version.getId());
        publishRecordRepository.save(new CmsPublishRecord(pageId, locale, version.getId(), currentUserId(), request == null ? null : request.remark()));
        page.touch();
        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    @Transactional
    public PageDetailResponse rollback(Long pageId, String locale, PublishPageRequest request) {
        log.info("Rolling back CMS page: {}, locale: {}, versionId: {}", pageId, locale, request.versionId());
        requirePage(pageId);
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (request == null || request.versionId() == null) {
            throw new IllegalArgumentException("versionId is required for rollback");
        }
        CmsPageVersion sourceVersion = versionRepository.findById(request.versionId())
                .orElseThrow(() -> new IllegalArgumentException("CMS source version not found"));
        if (!sourceVersion.getPageId().equals(pageId) || !sourceVersion.getLocale().equals(locale)) {
            throw new IllegalArgumentException("Version does not belong to this page/locale");
        }
        int nextVersion = versionRepository.findFirstByPageIdAndLocaleOrderByVersionNoDesc(pageId, locale)
                .map(v -> v.getVersionNo() + 1)
                .orElse(1);
        CmsPageVersion newVersion = new CmsPageVersion(pageId, locale, nextVersion,
                new LinkedHashMap<>(sourceVersion.getContentJson()), currentUserId(),
                request.remark() != null ? request.remark() : "rollback to version " + sourceVersion.getVersionNo());
        versionRepository.save(newVersion);
        translation.updateDraft(translation.getTitle(), translation.getSeoTitle(),
                translation.getSeoDescription(), translation.getSeoKeywords(), newVersion.getId());
        return toDetail(requirePage(pageId), translationRepository.findByPageId(pageId));
    }

    // ── Render ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RenderPageResponse render(String path, String locale) {
        log.info("Rendering CMS page: path={}, locale={}", path, locale);
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

    // ── Block Definitions ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BlockDefinitionResponse> listEnabledBlockDefinitions() {
        return blockDefinitionRepository.findByEnabledTrueOrderBySortOrderAsc().stream()
                .map(block -> new BlockDefinitionResponse(block.getType(), block.getName(), block.getSchemaJson(), block.getDefaultPropsJson()))
                .toList();
    }

    // ── Private helpers ────────────────────────────────────────

    private CmsPage requirePage(Long pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> {
                    log.error("CMS page not found: {}", pageId);
                    return new IllegalArgumentException("CMS page not found");
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildPublishSnapshot(Map<String, Object> contentJson) {
        Map<String, Object> snapshot = contentJson == null ? new LinkedHashMap<>() : new LinkedHashMap<>(contentJson);
        Object blocks = snapshot.get("blocks");
        if (blocks instanceof List<?> blockList) {
            snapshot.put("blocks", blockList.stream().map(this::enrichBlock).toList());
        }
        return snapshot;
    }

    @SuppressWarnings("unchecked")
    private Object enrichBlock(Object block) {
        if (!(block instanceof Map<?, ?> rawBlock)) {
            return block;
        }
        Map<String, Object> enrichedBlock = new LinkedHashMap<>((Map<String, Object>) rawBlock);
        if (!"pricing".equals(enrichedBlock.get("type")) || !(enrichedBlock.get("props") instanceof Map<?, ?> rawProps)) {
            return enrichedBlock;
        }
        Map<String, Object> props = new LinkedHashMap<>((Map<String, Object>) rawProps);
        Object planGroupCodeValue = props.get("planGroupCode");
        if (!(planGroupCodeValue instanceof String planGroupCode) || planGroupCode.isBlank()) {
            enrichedBlock.put("props", props);
            return enrichedBlock;
        }
        productService.findGroupByCode(planGroupCode)
                .ifPresentOrElse(
                        group -> {
                            props.put("planGroup", toPlanGroupMap(group));
                            props.put("plans", group.plans().stream().map(this::toPlanMap).toList());
                            props.put("dataStatus", "ready");
                            props.remove("dataError");
                        },
                        () -> {
                            props.put("plans", List.of());
                            props.put("dataStatus", "missing");
                            props.put("dataError", "Product plan group not found");
                        }
                );
        enrichedBlock.put("props", props);
        return enrichedBlock;
    }

    private Map<String, Object> toPlanGroupMap(PlanGroupResponse group) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", group.id());
        value.put("code", group.code());
        value.put("name", group.name());
        value.put("description", group.description());
        value.put("status", group.status());
        value.put("sortOrder", group.sortOrder());
        value.put("plans", group.plans().stream().map(this::toPlanMap).toList());
        return value;
    }

    private Map<String, Object> toPlanMap(PlanResponse plan) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", plan.id());
        value.put("code", plan.code());
        value.put("name", plan.name());
        value.put("description", plan.description());
        value.put("badge", plan.badge());
        value.put("status", plan.status());
        value.put("sortOrder", plan.sortOrder());
        value.put("prices", plan.prices().stream().map(this::toPriceMap).toList());
        value.put("features", plan.features().stream().map(this::toFeatureMap).toList());
        return value;
    }

    private Map<String, Object> toPriceMap(PriceResponse price) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", price.id());
        value.put("currency", price.currency());
        value.put("billingCycle", price.billingCycle());
        value.put("amount", price.amount());
        value.put("originalAmount", price.originalAmount());
        value.put("status", price.status());
        return value;
    }

    private Map<String, Object> toFeatureMap(FeatureResponse feature) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", feature.id());
        value.put("featureName", feature.featureName());
        value.put("featureValue", feature.featureValue());
        value.put("included", feature.included());
        value.put("sortOrder", feature.sortOrder());
        return value;
    }

    private PageDetailResponse toDetail(CmsPage page, List<CmsPageTranslation> translations) {
        return new PageDetailResponse(
                page.getId(),
                page.getSlug(),
                page.getPageType(),
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

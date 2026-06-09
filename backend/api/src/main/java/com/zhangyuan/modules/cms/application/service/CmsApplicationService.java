package com.zhangyuan.modules.cms.application.service;

import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.modules.cms.domain.model.CmsPage;
import com.zhangyuan.modules.cms.domain.model.PageTranslation;
import com.zhangyuan.modules.cms.domain.model.CmsPublishRecord;
import com.zhangyuan.modules.cms.domain.model.CmsBlockDefinition;
import com.zhangyuan.modules.cms.dto.BlockDefinitionResponse;
import com.zhangyuan.modules.cms.dto.CreateBlockDefinitionRequest;
import com.zhangyuan.modules.cms.dto.CreatePageRequest;
import com.zhangyuan.modules.cms.dto.UpdateBlockDefinitionRequest;
import com.zhangyuan.modules.cms.dto.PageDetailResponse;
import com.zhangyuan.modules.cms.dto.PageListItemResponse;
import com.zhangyuan.modules.cms.dto.PageTranslationResponse;
import com.zhangyuan.modules.cms.dto.PublishPageRequest;
import com.zhangyuan.modules.cms.dto.RenderPageResponse;
import com.zhangyuan.modules.cms.dto.SaveDraftRequest;
import com.zhangyuan.modules.cms.dto.UpdatePageRequest;
import com.zhangyuan.modules.cms.dto.VersionResponse;
import com.zhangyuan.modules.cms.domain.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.domain.repository.CmsTranslationRepository;
import com.zhangyuan.modules.cms.domain.repository.CmsVersionRepository;
import com.zhangyuan.modules.cms.domain.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.cms.domain.repository.CmsBlockDefinitionRepository;
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
    private final CmsTranslationRepository translationRepository;
    private final CmsVersionRepository versionRepository;
    private final CmsPublishRecordRepository publishRecordRepository;
    private final CmsBlockDefinitionRepository blockDefinitionRepository;
    private final ProductApplicationService productService;

    @org.springframework.beans.factory.annotation.Autowired
    public CmsApplicationService(CmsPageRepository pageRepository,
                                  CmsTranslationRepository translationRepository,
                                  CmsVersionRepository versionRepository,
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
    public PageResponse<PageListItemResponse> listPages(int page, int pageSize, String keyword) {
        var pageResult = (keyword != null && !keyword.isBlank())
                ? pageRepository.findByKeyword(keyword, page, pageSize)
                : pageRepository.findAll(page, pageSize);
        List<PageListItemResponse> items = pageResult.items().stream()
                .map(p -> new PageListItemResponse(p.getId(), p.getSlug(), p.getPageType(), p.getDefaultLocale(), p.getStatus(), p.getUpdatedAt()))
                .toList();
        return PageResponse.of(items, pageResult.page(), pageResult.pageSize(), pageResult.total());
    }

    @Transactional(readOnly = true)
    public PageResponse<PageListItemResponse> listPublishedByType(String type, int page, int pageSize) {
        String resolvedType = type == null || type.isBlank() ? "custom" : type;
        var pageResult = pageRepository.findByPageTypeAndStatus(resolvedType, CmsPage.STATUS_ENABLED, page, pageSize);
        List<PageListItemResponse> items = pageResult.items().stream()
                .map(p -> new PageListItemResponse(p.getId(), p.getSlug(), p.getPageType(), p.getDefaultLocale(), p.getStatus(), p.getUpdatedAt()))
                .toList();
        return PageResponse.of(items, pageResult.page(), pageResult.pageSize(), pageResult.total());
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
        var translation = translationRepository.create(page.getId(), locale, request.title());
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
        page.changeSlug(slug);
        if (request.defaultLocale() != null && !request.defaultLocale().isBlank()) {
            page.changeDefaultLocale(request.defaultLocale());
        }
        if (request.pageType() != null && !request.pageType().isBlank()) {
            page.changeType(request.pageType());
        }
        if (request.enabled() != null) {
            if (request.enabled()) {
                page.enable();
            } else {
                page.disable();
            }
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
        var translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseGet(() -> translationRepository.create(pageId, locale, request.title()));
        int nextVersion = versionRepository.findLatestByPageIdAndLocale(pageId, locale)
                .map(v -> v.getVersionNo() + 1)
                .orElse(1);
        var createdVersion = versionRepository.createAndSave(
                pageId, locale, nextVersion, request.content(), currentUserId(), request.remark());
        translation.updateDraftInfo(request.title(), request.seoTitle(), request.seoDescription(),
                request.seoKeywords(), createdVersion.getId());
        translationRepository.save(pageId, translation);
        page.touch();
        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDraftVersion(Long pageId, String locale) {
        var translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found for pageId=" + pageId + ", locale=" + locale));
        if (translation.getDraftVersionId() == null) {
            throw new IllegalArgumentException("CMS page translation has no draft version");
        }
        return versionRepository.getContentJson(translation.getDraftVersionId());
    }

    @Transactional(readOnly = true)
    public List<VersionResponse> listVersions(Long pageId, String locale) {
        return versionRepository.findByPageIdAndLocale(pageId, locale).stream()
                .map(v -> new VersionResponse(v.getId(), v.getVersionNo(), v.getCreatedAt(), v.getRemark()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> preview(Long pageId, String locale, Long versionId) {
        requirePage(pageId);
        if (versionId != null) {
            return versionRepository.getContentJson(versionId);
        }
        var translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (translation.getDraftVersionId() == null) {
            throw new IllegalArgumentException("CMS page has no draft version");
        }
        return versionRepository.getContentJson(translation.getDraftVersionId());
    }

    // ── Publish & Rollback ─────────────────────────────────────

    @Transactional
    public PageDetailResponse publish(Long pageId, String locale, PublishPageRequest request) {
        log.info("Publishing CMS page: {}, locale: {}", pageId, locale);
        CmsPage page = requirePage(pageId);
        var translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (translation.getDraftVersionId() == null) {
            throw new IllegalArgumentException("CMS page has no draft to publish");
        }
        var contentJson = versionRepository.getContentJson(translation.getDraftVersionId());
        var snapshot = buildPublishSnapshot(contentJson);
        versionRepository.publishSnapshot(translation.getDraftVersionId(), snapshot);
        translation.publish(translation.getDraftVersionId());
        translationRepository.save(pageId, translation);
        publishRecordRepository.save(new CmsPublishRecord(
                pageId, locale, translation.getDraftVersionId(), currentUserId(),
                request == null ? null : request.remark()));
        page.touch();
        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    @Transactional
    public PageDetailResponse rollback(Long pageId, String locale, PublishPageRequest request) {
        log.info("Rolling back CMS page: {}, locale: {}, versionId: {}", pageId, locale, request.versionId());
        requirePage(pageId);
        var translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (request == null || request.versionId() == null) {
            throw new IllegalArgumentException("versionId is required for rollback");
        }
        if (!versionRepository.versionBelongsToPage(request.versionId(), pageId, locale)) {
            throw new IllegalArgumentException("Version does not belong to this page/locale");
        }
        var sourceContent = versionRepository.getContentJson(request.versionId());
        int nextVersion = versionRepository.findLatestByPageIdAndLocale(pageId, locale)
                .map(v -> v.getVersionNo() + 1)
                .orElse(1);
        var newVersion = versionRepository.createAndSave(pageId, locale, nextVersion,
                new LinkedHashMap<>(sourceContent), currentUserId(),
                request.remark() != null ? request.remark() : "rollback to version " + nextVersion);
        translation.updateDraftInfo(translation.getTitle(), translation.getSeoTitle(),
                translation.getSeoDescription(), translation.getSeoKeywords(), newVersion.getId());
        translationRepository.save(pageId, translation);
        return toDetail(requirePage(pageId), translationRepository.findByPageId(pageId));
    }

    // ── Render ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RenderPageResponse render(String path, String locale) {
        log.info("Rendering CMS page: path={}, locale={}", path, locale);
        CmsPage page = pageRepository.findBySlug(CmsPage.normalizeSlug(path))
                .orElseThrow(() -> new IllegalArgumentException("CMS page not found"));
        String resolvedLocale = locale == null || locale.isBlank() ? page.getDefaultLocale() : locale;
        var translation = translationRepository.findByPageIdAndLocale(page.getId(), resolvedLocale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (translation.getPublishedVersionId() == null) {
            throw new IllegalArgumentException("CMS page is not published");
        }
        var snapshot = versionRepository.getSnapshotJson(translation.getPublishedVersionId());
        if (snapshot == null) {
            snapshot = versionRepository.getContentJson(translation.getPublishedVersionId());
        }
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
        return blockDefinitionRepository.findEnabled().stream()
                .map(block -> new BlockDefinitionResponse(block.getId(), block.getType(), block.getName(), block.getSchemaJson(), block.getDefaultPropsJson(), block.isEnabled(), block.getSortOrder(), block.getCreatedAt(), block.getUpdatedAt()))
                .toList();
    }

    @Transactional
    public BlockDefinitionResponse createBlockDefinition(CreateBlockDefinitionRequest request) {
        log.info("Creating block definition: type={}", request.type());
        if (blockDefinitionRepository.findByType(request.type()).isPresent()) {
            log.warn("Block definition type already exists: {}", request.type());
            throw new IllegalArgumentException("Block definition type already exists");
        }
        CmsBlockDefinition block = new CmsBlockDefinition(
                request.type(), request.name(), request.schema(), request.defaultProps(), request.sortOrder());
        CmsBlockDefinition saved = blockDefinitionRepository.save(block);
        log.info("Block definition created: id={}, type={}", saved.getId(), saved.getType());
        return toBlockDefinitionResponse(saved);
    }

    @Transactional
    public BlockDefinitionResponse updateBlockDefinition(Long id, UpdateBlockDefinitionRequest request) {
        log.info("Updating block definition: {}", id);
        CmsBlockDefinition block = blockDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Block definition not found"));
        if (request.type() != null && !request.type().isBlank()) {
            if (!request.type().equals(block.getType()) && blockDefinitionRepository.findByType(request.type()).isPresent()) {
                throw new IllegalArgumentException("Block definition type already exists");
            }
            block = new CmsBlockDefinition(request.type(), block.getName(), block.getSchemaJson(), block.getDefaultPropsJson(), block.getSortOrder());
            block.setId(id);
            block.setEnabled(block.isEnabled());
        }
        if (request.name() != null && !request.name().isBlank()) {
            block = new CmsBlockDefinition(block.getType(), request.name(), block.getSchemaJson(), block.getDefaultPropsJson(), block.getSortOrder());
            block.setId(id);
            block.setEnabled(block.isEnabled());
        }
        if (request.schema() != null) {
            block = new CmsBlockDefinition(block.getType(), block.getName(), request.schema(), block.getDefaultPropsJson(), block.getSortOrder());
            block.setId(id);
            block.setEnabled(block.isEnabled());
        }
        if (request.defaultProps() != null) {
            block = new CmsBlockDefinition(block.getType(), block.getName(), block.getSchemaJson(), request.defaultProps(), block.getSortOrder());
            block.setId(id);
            block.setEnabled(block.isEnabled());
        }
        if (request.sortOrder() != null) {
            block = new CmsBlockDefinition(block.getType(), block.getName(), block.getSchemaJson(), block.getDefaultPropsJson(), request.sortOrder());
            block.setId(id);
            block.setEnabled(block.isEnabled());
        }
        CmsBlockDefinition saved = blockDefinitionRepository.save(block);
        log.info("Block definition updated: id={}, type={}", saved.getId(), saved.getType());
        return toBlockDefinitionResponse(saved);
    }

    @Transactional
    public void deleteBlockDefinition(Long id) {
        log.info("Soft-deleting block definition: {}", id);
        CmsBlockDefinition block = blockDefinitionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Block definition not found"));
        block.setEnabled(false);
        blockDefinitionRepository.save(block);
        log.info("Block definition soft-deleted: {}", id);
    }

    private BlockDefinitionResponse toBlockDefinitionResponse(CmsBlockDefinition block) {
        return new BlockDefinitionResponse(
                block.getId(), block.getType(), block.getName(),
                block.getSchemaJson(), block.getDefaultPropsJson(),
                block.isEnabled(), block.getSortOrder(),
                block.getCreatedAt(), block.getUpdatedAt());
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

    private PageDetailResponse toDetail(CmsPage page, List<PageTranslation> translations) {
        return new PageDetailResponse(
                page.getId(),
                page.getSlug(),
                page.getPageType(),
                page.getDefaultLocale(),
                page.getStatus(),
                translations.stream().map(this::toTranslation).toList()
        );
    }

    private PageTranslationResponse toTranslation(PageTranslation translation) {
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
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser user) {
            return user.getId();
        }
        return null;
    }
}

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
import com.zhangyuan.modules.cms.dto.UpdatePageRequest;
import com.zhangyuan.modules.cms.dto.VersionResponse;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.product.ProductService;
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

/**
 * CMS 服务，提供页面管理、翻译、版本控制、发布和渲染的核心功能。
 */
@Service
public class CmsService {

    private static final Logger log = LoggerFactory.getLogger(CmsService.class);

    private final CmsPageRepository pageRepository;
    private final CmsPageTranslationRepository translationRepository;
    private final CmsPageVersionRepository versionRepository;
    private final CmsPublishRecordRepository publishRecordRepository;
    private final ProductService productService;

    public CmsService(CmsPageRepository pageRepository, CmsPageTranslationRepository translationRepository,
                      CmsPageVersionRepository versionRepository, CmsPublishRecordRepository publishRecordRepository,
                      ProductService productService) {
        this.pageRepository = pageRepository;
        this.translationRepository = translationRepository;
        this.versionRepository = versionRepository;
        this.publishRecordRepository = publishRecordRepository;
        this.productService = productService;
    }

    /**
     * 获取所有 CMS 页面列表。
     *
     * @return 页面列表项响应列表
     */
    @Transactional(readOnly = true)
    public List<PageListItemResponse> listPages() {
        return pageRepository.findAll().stream()
                .map(page -> new PageListItemResponse(page.getId(), page.getSlug(), page.getDefaultLocale(), page.getStatus(), page.getUpdatedAt()))
                .toList();
    }

    /**
     * 创建 CMS 页面及初始翻译。
     *
     * @param request 创建页面请求
     * @return 页面详情
     */
    @Transactional
    public PageDetailResponse createPage(CreatePageRequest request) {
        log.info("Creating CMS page: slug={}, title={}", request.slug(), request.title());
        String slug = CmsPage.normalizeSlug(request.slug());
        // 检查 slug 唯一性
        if (pageRepository.existsBySlug(slug)) {
            log.warn("CMS page slug already exists: {}", slug);
            throw new IllegalArgumentException("CMS page slug already exists");
        }

        String locale = request.defaultLocale() == null || request.defaultLocale().isBlank() ? "zh-CN" : request.defaultLocale();
        CmsPage page = pageRepository.save(new CmsPage(slug, locale, currentUserId()));
        CmsPageTranslation translation = translationRepository.save(new CmsPageTranslation(page.getId(), locale, request.title()));
        log.info("CMS page created: id={}, slug={}", page.getId(), page.getSlug());
        return toDetail(page, List.of(translation));
    }

    /**
     * 根据 ID 查询页面详情。
     *
     * @param pageId 页面 ID
     * @return 页面详情
     */
    @Transactional(readOnly = true)
    public PageDetailResponse getPage(Long pageId) {
        CmsPage page = requirePage(pageId);
        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    /**
     * 更新页面基本信息（slug、默认语言）。
     *
     * @param pageId  页面 ID
     * @param request 更新页面请求
     * @return 更新后的页面详情
     */
    @Transactional
    public PageDetailResponse updatePage(Long pageId, UpdatePageRequest request) {
        log.info("Updating CMS page: {}", pageId);
        CmsPage page = requirePage(pageId);
        String slug = CmsPage.normalizeSlug(request.slug());
        // 如果 slug 变化则检查唯一性
        if (!slug.equals(page.getSlug()) && pageRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("CMS page slug already exists");
        }
        page.setSlug(slug);
        if (request.defaultLocale() != null && !request.defaultLocale().isBlank()) {
            page.setDefaultLocale(request.defaultLocale());
        }
        page.touch();
        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    /**
     * 删除页面及关联的翻译、版本和发布记录。
     *
     * @param pageId 页面 ID
     */
    @Transactional
    public void deletePage(Long pageId) {
        log.info("Deleting CMS page: {}", pageId);
        CmsPage page = requirePage(pageId);
        // 级联删除关联数据
        List<CmsPageTranslation> translations = translationRepository.findByPageId(pageId);
        for (CmsPageTranslation t : translations) {
            versionRepository.deleteByPageIdAndLocale(pageId, t.getLocale());
            publishRecordRepository.deleteByPageIdAndLocale(pageId, t.getLocale());
        }
        translationRepository.deleteByPageId(pageId);
        pageRepository.delete(page);
    }

    /**
     * 保存草稿版本。
     *
     * @param pageId  页面 ID
     * @param locale  语言环境
     * @param request 保存草稿请求
     * @return 更新后的页面详情
     */
    @Transactional
    public PageDetailResponse saveDraft(Long pageId, String locale, SaveDraftRequest request) {
        log.info("Saving draft for CMS page: {}, locale: {}", pageId, locale);
        CmsPage page = requirePage(pageId);
        // 查找或创建翻译
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(pageId, locale)
                .orElseGet(() -> translationRepository.save(new CmsPageTranslation(pageId, locale, request.title())));

        // 计算下一个版本号
        int nextVersion = versionRepository.findFirstByPageIdAndLocaleOrderByVersionNoDesc(pageId, locale)
                .map(version -> version.getVersionNo() + 1)
                .orElse(1);
        CmsPageVersion version = versionRepository.save(new CmsPageVersion(pageId, locale, nextVersion, request.content(), currentUserId(), request.remark()));
        translation.updateDraft(request.title(), request.seoTitle(), request.seoDescription(), request.seoKeywords(), version.getId());
        page.touch();

        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    /**
     * 发布草稿版本，生成发布快照。
     *
     * @param pageId  页面 ID
     * @param locale  语言环境
     * @param request 发布请求（可选）
     * @return 更新后的页面详情
     */
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
        // 构建发布快照（含产品数据）
        version.publishSnapshot(buildPublishSnapshot(version.getContentJson()));
        translation.publish(version.getId());
        publishRecordRepository.save(new CmsPublishRecord(pageId, locale, version.getId(), currentUserId(), request == null ? null : request.remark()));
        page.touch();

        return toDetail(page, translationRepository.findByPageId(pageId));
    }

    /**
     * 获取当前草稿版本内容。
     *
     * @param pageId 页面 ID
     * @param locale 语言环境
     * @return 草稿内容
     */
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

    /**
     * 获取页面版本历史列表。
     *
     * @param pageId 页面 ID
     * @param locale 语言环境
     * @return 版本响应列表
     */
    @Transactional(readOnly = true)
    public List<VersionResponse> listVersions(Long pageId, String locale) {
        return versionRepository.findByPageIdAndLocaleOrderByVersionNoDesc(pageId, locale)
                .stream()
                .map(v -> new VersionResponse(v.getId(), v.getVersionNo(), v.getCreatedAt(), v.getRemark()))
                .toList();
    }

    /**
     * 预览页面内容（可指定版本或使用当前草稿）。
     *
     * @param pageId   页面 ID
     * @param locale   语言环境
     * @param versionId 版本 ID（可选）
     * @return 页面内容
     */
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

    /**
     * 回滚到指定的历史版本。
     *
     * @param pageId  页面 ID
     * @param locale  语言环境
     * @param request 发布请求（含 versionId）
     * @return 更新后的页面详情
     */
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
        // 验证版本属于当前页面和语言
        if (!sourceVersion.getPageId().equals(pageId) || !sourceVersion.getLocale().equals(locale)) {
            throw new IllegalArgumentException("Version does not belong to this page/locale");
        }

        // 基于源版本创建新版本
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

    /**
     * 渲染已发布的页面，用于前台展示。
     *
     * @param path   页面路径
     * @param locale 语言环境
     * @return 渲染后的页面响应
     */
    @Transactional(readOnly = true)
    public RenderPageResponse render(String path, String locale) {
        log.info("Rendering CMS page: path={}, locale={}", path, locale);
        CmsPage page = pageRepository.findBySlug(CmsPage.normalizeSlug(path))
                .orElseThrow(() -> new IllegalArgumentException("CMS page not found"));
        // 解析语言环境，默认使用页面默认语言
        String resolvedLocale = locale == null || locale.isBlank() ? page.getDefaultLocale() : locale;
        CmsPageTranslation translation = translationRepository.findByPageIdAndLocale(page.getId(), resolvedLocale)
                .orElseThrow(() -> new IllegalArgumentException("CMS page translation not found"));
        if (translation.getPublishedVersionId() == null) {
            throw new IllegalArgumentException("CMS page is not published");
        }

        CmsPageVersion version = versionRepository.findById(translation.getPublishedVersionId())
                .orElseThrow(() -> new IllegalArgumentException("CMS published version not found"));
        // 优先使用发布快照，其次使用内容
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

    /**
     * 查找页面，不存在则抛出异常。
     */
    private CmsPage requirePage(Long pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> {
                    log.error("CMS page not found: {}", pageId);
                    return new IllegalArgumentException("CMS page not found");
                });
    }

    /**
     * 构建发布快照，对 pricing 区块进行数据富化。
     */
    private Map<String, Object> buildPublishSnapshot(Map<String, Object> contentJson) {
        Map<String, Object> snapshot = contentJson == null ? new LinkedHashMap<>() : new LinkedHashMap<>(contentJson);
        Object blocks = snapshot.get("blocks");
        // 对每个区块进行富化处理
        if (blocks instanceof List<?> blockList) {
            snapshot.put("blocks", blockList.stream().map(this::enrichBlock).toList());
        }
        return snapshot;
    }

    /**
     * 富化区块数据，对 pricing 类型区块注入产品方案数据。
     */
    @SuppressWarnings("unchecked")
    private Object enrichBlock(Object block) {
        if (!(block instanceof Map<?, ?> rawBlock)) {
            return block;
        }
        Map<String, Object> enrichedBlock = new LinkedHashMap<>((Map<String, Object>) rawBlock);
        // 仅处理 pricing 类型区块
        if (!"pricing".equals(enrichedBlock.get("type")) || !(enrichedBlock.get("props") instanceof Map<?, ?> rawProps)) {
            return enrichedBlock;
        }

        Map<String, Object> props = new LinkedHashMap<>((Map<String, Object>) rawProps);
        Object planGroupCodeValue = props.get("planGroupCode");
        if (!(planGroupCodeValue instanceof String planGroupCode) || planGroupCode.isBlank()) {
            enrichedBlock.put("props", props);
            return enrichedBlock;
        }

        // 根据 planGroupCode 查询产品数据并注入
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

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID，未登录返回 null
     */
    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            return null;
        }
        return user.getId();
    }
}

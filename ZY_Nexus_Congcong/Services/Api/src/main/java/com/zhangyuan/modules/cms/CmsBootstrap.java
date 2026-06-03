package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.domain.CmsBlockDefinition;
import com.zhangyuan.modules.cms.domain.CmsPage;
import com.zhangyuan.modules.cms.domain.CmsPageTranslation;
import com.zhangyuan.modules.cms.domain.CmsPageVersion;
import com.zhangyuan.modules.cms.domain.CmsPublishRecord;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.system.domain.SystemSetting;
import com.zhangyuan.modules.system.repository.SystemSettingRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@Profile("!test")
public class CmsBootstrap implements ApplicationRunner {

    private final CmsBlockDefinitionRepository blockDefinitionRepository;
    private final CmsPageRepository pageRepository;
    private final CmsPageTranslationRepository translationRepository;
    private final CmsPageVersionRepository versionRepository;
    private final CmsPublishRecordRepository publishRecordRepository;
    private final SystemSettingRepository systemSettingRepository;

    public CmsBootstrap(CmsBlockDefinitionRepository blockDefinitionRepository,
                        CmsPageRepository pageRepository,
                        CmsPageTranslationRepository translationRepository,
                        CmsPageVersionRepository versionRepository,
                        CmsPublishRecordRepository publishRecordRepository,
                        SystemSettingRepository systemSettingRepository) {
        this.blockDefinitionRepository = blockDefinitionRepository;
        this.pageRepository = pageRepository;
        this.translationRepository = translationRepository;
        this.versionRepository = versionRepository;
        this.publishRecordRepository = publishRecordRepository;
        this.systemSettingRepository = systemSettingRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        defaultBlocks().forEach(seed -> blockDefinitionRepository.findByType(seed.type())
                .orElseGet(() -> blockDefinitionRepository.save(new CmsBlockDefinition(seed.type(), seed.name(), seed.schema(), seed.defaultProps(), seed.sortOrder()))));

        seedDefaultSettings();
        seedPage("/", "首页");
        seedPage("/plans", "套餐");
    }

    private void seedDefaultSettings() {
        seedSetting("site_name", "ZHANGYUAN API");
        seedSetting("site_description", "新一代 API 服务平台");
        seedSetting("icp_filing", "沪ICP备XXXXXXXX号");
        seedSetting("footer_text", "© 2026 ZHANGYUAN. All rights reserved.");
    }

    private void seedSetting(String key, String value) {
        systemSettingRepository.findBySettingKey(key)
                .orElseGet(() -> systemSettingRepository.save(new SystemSetting(key, value)));
    }

    private void seedPage(String slug, String title) {
        if (pageRepository.findBySlug(slug).isPresent()) {
            return;
        }

        String locale = "zh-CN";
        CmsPage page = pageRepository.save(new CmsPage(slug, locale, null));
        int versionNo = versionRepository.findFirstByPageIdAndLocaleOrderByVersionNoDesc(page.getId(), locale)
                .map(v -> v.getVersionNo() + 1)
                .orElse(1);
        Map<String, Object> content = pageContent(slug);
        CmsPageVersion version = versionRepository.save(new CmsPageVersion(page.getId(), locale, versionNo, content, null, "系统初始版本"));
        version.publishSnapshot(content);

        CmsPageTranslation translation = translationRepository.save(new CmsPageTranslation(page.getId(), locale, title));
        translation.publish(version.getId());
        publishRecordRepository.save(new CmsPublishRecord(page.getId(), locale, version.getId(), null, "系统初始发布"));
        page.touch();
    }

    private Map<String, Object> pageContent(String slug) {
        if ("/plans".equals(slug)) {
            return plansPageContent();
        }
        return homePageContent();
    }

    private Map<String, Object> homePageContent() {
        return Map.of(
                "layout", "default",
                "blocks", List.of(
                        Map.of("id", "hero_home", "type", "hero", "props", Map.of(
                                "title", "新一代 API 服务平台",
                                "subtitle", "为开发者和企业提供稳定、高性能、可扩展的 API 网关与管理服务",
                                "primaryButtonText", "查看套餐",
                                "primaryButtonUrl", "/plans"
                        )),
                        Map.of("id", "features_home", "type", "feature-grid", "props", Map.of(
                                "title", "核心功能",
                                "items", List.of(
                                        Map.of("name", "智能路由", "description", "基于权重的灰度发布与多版本路由"),
                                        Map.of("name", "实时监控", "description", "毫秒级延迟监控与自动告警"),
                                        Map.of("name", "安全防护", "description", "WAF、限流、IP 黑白名单全方位防护"),
                                        Map.of("name", "弹性伸缩", "description", "按需自动扩容，应对流量波峰"),
                                        Map.of("name", "多协议支持", "description", "REST、gRPC、WebSocket 统一接入"),
                                        Map.of("name", "开发者工具", "description", "在线调试、SDK 自动生成、文档管理")
                                )
                        )),
                        Map.of("id", "pricing_home", "type", "pricing", "props", Map.of(
                                "planGroupCode", "api_plans",
                                "defaultBillingCycle", "monthly"
                        )),
                        Map.of("id", "faq_home", "type", "faq", "props", Map.of(
                                "title", "常见问题",
                                "items", List.of(
                                        Map.of("question", "如何开始使用？", "answer", "注册账号后即可在控制台创建应用并获取 API Key。"),
                                        Map.of("question", "支持哪些支付方式？", "answer", "支持支付宝、微信支付和企业对公转账。"),
                                        Map.of("question", "是否提供免费额度？", "answer", "每个新用户赠送 1,000 次免费调用额度。"),
                                        Map.of("question", "如何升级套餐？", "answer", "在控制台可随时升级，差价按天折算。")
                                )
                        )),
                        Map.of("id", "cta_home", "type", "cta", "props", Map.of(
                                "title", "准备好开始了吗？",
                                "buttonText", "30 天免费试用",
                                "buttonUrl", "#"
                        ))
                )
        );
    }

    private Map<String, Object> plansPageContent() {
        return Map.of(
                "layout", "default",
                "blocks", List.of(
                        Map.of("id", "hero_plans", "type", "hero", "props", Map.of(
                                "title", "选择适合您的套餐",
                                "subtitle", "灵活定价，按需付费，支持随时升级",
                                "primaryButtonText", "开始免费试用",
                                "primaryButtonUrl", "#"
                        )),
                        Map.of("id", "pricing_plans", "type", "pricing", "props", Map.of(
                                "planGroupCode", "api_plans",
                                "defaultBillingCycle", "monthly"
                        )),
                        Map.of("id", "faq_plans", "type", "faq", "props", Map.of(
                                "title", "套餐常见问题",
                                "items", List.of(
                                        Map.of("question", "可以随时切换套餐吗？", "answer", "是的，您可以随时升级或降级套餐，差价按天折算。"),
                                        Map.of("question", "是否有长期折扣？", "answer", "年付套餐享受 8 折优惠，详情请查看价格对比。"),
                                        Map.of("question", "超出配额如何处理？", "answer", "超出部分按量计费，具体费率请查看套餐详情。")
                                )
                        ))
                )
        );
    }

    private List<BlockSeed> defaultBlocks() {
        return List.of(
                new BlockSeed("hero", "Hero", fields("title", "text", "subtitle", "textarea"), Map.of(), 10),
                new BlockSeed("pricing", "Pricing", fields("planGroupCode", "text", "defaultBillingCycle", "text"), Map.of("defaultBillingCycle", "monthly"), 20),
                new BlockSeed("feature-grid", "Feature Grid", fields("title", "text", "items", "list"), Map.of(), 30),
                new BlockSeed("faq", "FAQ", fields("title", "text", "items", "list"), Map.of(), 40),
                new BlockSeed("cta", "CTA", fields("title", "text", "buttonText", "text", "buttonUrl", "url"), Map.of(), 50),
                new BlockSeed("rich-text", "Rich Text", fields("content", "rich-text"), Map.of(), 60)
        );
    }

    private Map<String, Object> fields(String... pairs) {
        java.util.ArrayList<Map<String, Object>> fields = new java.util.ArrayList<>();
        for (int i = 0; i < pairs.length; i += 2) {
            fields.add(Map.of("key", pairs[i], "label", pairs[i], "type", pairs[i + 1]));
        }
        return Map.of("fields", fields);
    }

    private record BlockSeed(String type, String name, Map<String, Object> schema, Map<String, Object> defaultProps, Integer sortOrder) {
    }
}

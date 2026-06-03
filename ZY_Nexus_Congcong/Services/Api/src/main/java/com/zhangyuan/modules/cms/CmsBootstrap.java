package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.domain.CmsBlockDefinition;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
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

    private final CmsBlockDefinitionRepository repository;

    public CmsBootstrap(CmsBlockDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        defaultBlocks().forEach(seed -> repository.findByType(seed.type())
                .orElseGet(() -> repository.save(new CmsBlockDefinition(seed.type(), seed.name(), seed.schema(), seed.defaultProps(), seed.sortOrder()))));
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

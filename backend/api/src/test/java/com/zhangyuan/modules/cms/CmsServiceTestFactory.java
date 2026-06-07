package com.zhangyuan.modules.cms;

import com.zhangyuan.modules.cms.adapter.out.persistence.JpaCmsBlockDefinitionRepository;
import com.zhangyuan.modules.cms.adapter.out.persistence.JpaCmsPageRepository;
import com.zhangyuan.modules.cms.adapter.out.persistence.JpaCmsPublishRecordRepository;
import com.zhangyuan.modules.cms.adapter.out.persistence.JpaCmsTranslationRepository;
import com.zhangyuan.modules.cms.adapter.out.persistence.JpaCmsVersionRepository;
import com.zhangyuan.modules.cms.application.service.CmsApplicationService;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import com.zhangyuan.modules.product.application.service.ProductApplicationService;

public class CmsServiceTestFactory {

    public static CmsApplicationService create(CmsPageRepository jpaPageRepo,
                                                CmsPageTranslationRepository jpaTranslationRepo,
                                                CmsPageVersionRepository jpaVersionRepo,
                                                CmsPublishRecordRepository jpaPublishRecordRepo,
                                                CmsBlockDefinitionRepository jpaBlockDefRepo,
                                                ProductApplicationService productService) {
        return new CmsApplicationService(
                new JpaCmsPageRepository(jpaPageRepo),
                new JpaCmsTranslationRepository(jpaTranslationRepo),
                new JpaCmsVersionRepository(jpaVersionRepo),
                new JpaCmsPublishRecordRepository(jpaPublishRecordRepo),
                new JpaCmsBlockDefinitionRepository(jpaBlockDefRepo),
                productService);
    }
}

package com.zhangyuan;

import com.zhangyuan.modules.auth.repository.AdminPermissionRepository;
import com.zhangyuan.modules.auth.repository.AdminRoleRepository;
import com.zhangyuan.modules.auth.repository.AdminUserRepository;
import com.zhangyuan.modules.cms.repository.CmsBlockDefinitionRepository;
import com.zhangyuan.modules.cms.repository.CmsPageRepository;
import com.zhangyuan.modules.cms.repository.CmsPageTranslationRepository;
import com.zhangyuan.modules.cms.repository.CmsPageVersionRepository;
import com.zhangyuan.modules.cms.repository.CmsPublishRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ZhangyuanApplicationTests {

    @MockBean
    private AdminUserRepository adminUserRepository;

    @MockBean
    private AdminRoleRepository adminRoleRepository;

    @MockBean
    private AdminPermissionRepository adminPermissionRepository;

    @MockBean
    private CmsPageRepository cmsPageRepository;

    @MockBean
    private CmsPageTranslationRepository cmsPageTranslationRepository;

    @MockBean
    private CmsPageVersionRepository cmsPageVersionRepository;

    @MockBean
    private CmsPublishRecordRepository cmsPublishRecordRepository;

    @MockBean
    private CmsBlockDefinitionRepository cmsBlockDefinitionRepository;

    @Test
    void contextLoads() {
    }
}

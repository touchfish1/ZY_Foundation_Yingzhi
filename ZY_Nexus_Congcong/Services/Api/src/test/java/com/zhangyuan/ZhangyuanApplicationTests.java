package com.zhangyuan;

import com.zhangyuan.modules.auth.repository.AdminPermissionRepository;
import com.zhangyuan.modules.auth.repository.AdminRoleRepository;
import com.zhangyuan.modules.auth.repository.AdminUserRepository;
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

    @Test
    void contextLoads() {
    }
}

package com.zhangyuan.auth.common.security;

import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import com.zhangyuan.auth.adapter.out.persistence.AdminRole;
import com.zhangyuan.auth.adapter.out.persistence.AdminUser;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import com.zhangyuan.auth.repository.AdminRoleRepository;
import com.zhangyuan.auth.repository.AdminUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("!test")
public class AuthBootstrap implements ApplicationRunner {

    private final SecurityProperties securityProperties;
    private final AdminUserRepository userRepository;
    private final AdminRoleRepository roleRepository;
    private final AdminPermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthBootstrap(SecurityProperties securityProperties, AdminUserRepository userRepository, AdminRoleRepository roleRepository,
                         AdminPermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.securityProperties = securityProperties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<AdminPermission> permissions = defaultPermissions().stream()
                .map(seed -> {
                    AdminPermission perm = permissionRepository.findByCode(seed.code())
                            .orElseGet(() -> new AdminPermission(seed.code(), seed.name(), seed.module()));
                    // Update name/module for existing permissions
                    perm.setName(seed.name());
                    perm.setModule(seed.module());
                    return permissionRepository.save(perm);
                })
                .toList();

        AdminRole superAdmin = roleRepository.findByCode("super_admin")
                .orElseGet(() -> roleRepository.save(new AdminRole("super_admin", "Super Admin")));
        superAdmin.getPermissions().addAll(permissions);

        String username = securityProperties.getDefaultAdmin().getUsername();
        if (!userRepository.existsByUsername(username)) {
            AdminUser admin = new AdminUser(
                    username,
                    passwordEncoder.encode(securityProperties.getDefaultAdmin().getPassword()),
                    securityProperties.getDefaultAdmin().getNickname()
            );
            admin.getRoles().add(superAdmin);
            userRepository.save(admin);
        }
    }

    private List<PermissionSeed> defaultPermissions() {
        return List.of(
                new PermissionSeed("cms:manage", "CMS 管理", "cms"),
                new PermissionSeed("cms:page:read", "查看页面", "cms"),
                new PermissionSeed("cms:page:create", "新建页面", "cms"),
                new PermissionSeed("cms:page:update", "编辑页面", "cms"),
                new PermissionSeed("cms:page:publish", "发布页面", "cms"),
                new PermissionSeed("cms:page:delete", "删除页面", "cms"),
                new PermissionSeed("asset:file:upload", "上传文件", "asset"),
                new PermissionSeed("asset:file:delete", "删除文件", "asset"),
                new PermissionSeed("asset:list", "文件列表", "asset"),
                new PermissionSeed("product:manage", "管理产品", "product"),
                new PermissionSeed("product:plan:read", "查看套餐", "product"),
                new PermissionSeed("product:plan:update", "编辑套餐", "product"),
                new PermissionSeed("order:read", "查看订单", "order"),
                new PermissionSeed("order:list", "订单列表", "order"),
                new PermissionSeed("payment:transaction:read", "查看支付", "payment"),
                new PermissionSeed("payment:list", "支付列表", "payment"),
                new PermissionSeed("system:user:manage", "管理用户", "system"),
                new PermissionSeed("system:user:list", "用户列表", "system"),
                new PermissionSeed("system:role:manage", "管理角色", "system"),
                new PermissionSeed("system:role:list", "角色列表", "system"),
                new PermissionSeed("system:permission:list", "权限列表", "system"),
                new PermissionSeed("system:permission:create", "新建权限", "system"),
                new PermissionSeed("system:permission:update", "编辑权限", "system"),
                new PermissionSeed("system:permission:delete", "删除权限", "system"),
                new PermissionSeed("system:user:update", "编辑用户", "system"),
                new PermissionSeed("system:role:update", "编辑角色", "system"),
                new PermissionSeed("system:menu:list", "菜单列表", "system"),
                new PermissionSeed("system:menu:create", "新建菜单", "system"),
                new PermissionSeed("system:menu:update", "编辑菜单", "system"),
                new PermissionSeed("system:menu:delete", "删除菜单", "system"),
                new PermissionSeed("system:setting:list", "设置列表", "system")
        );
    }

    private record PermissionSeed(String code, String name, String module) {
    }
}

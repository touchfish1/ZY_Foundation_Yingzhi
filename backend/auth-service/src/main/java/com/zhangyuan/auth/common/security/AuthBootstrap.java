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
                .map(permission -> permissionRepository.findByCode(permission.code())
                        .orElseGet(() -> permissionRepository.save(new AdminPermission(permission.code(), permission.name(), permission.module()))))
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
                new PermissionSeed("cms:page:read", "Read CMS pages", "cms"),
                new PermissionSeed("cms:page:create", "Create CMS pages", "cms"),
                new PermissionSeed("cms:page:update", "Update CMS pages", "cms"),
                new PermissionSeed("cms:page:publish", "Publish CMS pages", "cms"),
                new PermissionSeed("cms:page:delete", "Delete CMS pages", "cms"),
                new PermissionSeed("asset:file:upload", "Upload assets", "asset"),
                new PermissionSeed("asset:file:delete", "Delete assets", "asset"),
                new PermissionSeed("product:plan:read", "Read plans", "product"),
                new PermissionSeed("product:plan:update", "Update plans", "product"),
                new PermissionSeed("order:read", "Read orders", "order"),
                new PermissionSeed("payment:transaction:read", "Read payment transactions", "payment"),
                new PermissionSeed("system:user:manage", "Manage users", "system"),
                new PermissionSeed("system:role:manage", "Manage roles", "system")
        );
    }

    private record PermissionSeed(String code, String name, String module) {
    }
}

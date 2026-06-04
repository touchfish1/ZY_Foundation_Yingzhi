package com.zhangyuan.auth.common.security;

import com.zhangyuan.auth.adapter.out.persistence.AdminMenu;
import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import com.zhangyuan.auth.repository.AdminMenuRepository;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Profile("!test")
public class MenuBootstrap implements ApplicationRunner {

    private final AdminMenuRepository menuRepository;
    private final AdminPermissionRepository permissionRepository;

    public MenuBootstrap(AdminMenuRepository menuRepository, AdminPermissionRepository permissionRepository) {
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (menuRepository.count() > 0) {
            return;
        }

        // Top-level menus
        AdminMenu dashboard = menuRepository.save(createMenu(null, "Dashboard", "page", 1, "/", null, null));

        AdminMenu contentManagement = menuRepository.save(createMenu(null, "Content Management", "group", 2, null, null, null));
        AdminMenu businessManagement = menuRepository.save(createMenu(null, "Business Management", "group", 3, null, null, null));
        AdminMenu systemManagement = menuRepository.save(createMenu(null, "System Management", "group", 4, null, null, null));

        // --- Content Management children ---
        menuRepository.save(createMenu(contentManagement.getId(), "Pages", "page", 1, "/cms/pages", null, null));
        menuRepository.save(createMenu(contentManagement.getId(), "Media Assets", "page", 2, "/assets", null, null));

        // --- Business Management children ---
        menuRepository.save(createMenu(businessManagement.getId(), "Plan Groups", "page", 1, "/products/plan-groups", null, null));
        menuRepository.save(createMenu(businessManagement.getId(), "Plans", "page", 2, "/products/plans", null, null));
        menuRepository.save(createMenu(businessManagement.getId(), "Orders", "page", 3, "/orders", null, null));
        menuRepository.save(createMenu(businessManagement.getId(), "Payments", "page", 4, "/payments/transactions", null, null));

        // --- System Management children ---
        AdminMenu users = menuRepository.save(createMenu(systemManagement.getId(), "Users", "page", 1, "/system/users", null,
                permissionSet("system:user:list")));
        AdminMenu roles = menuRepository.save(createMenu(systemManagement.getId(), "Roles", "page", 2, "/system/roles", null,
                permissionSet("system:role:list")));
        AdminMenu permissions = menuRepository.save(createMenu(systemManagement.getId(), "Permissions", "page", 3, "/system/permissions", null,
                permissionSet("system:permission:list")));
        AdminMenu menus = menuRepository.save(createMenu(systemManagement.getId(), "Menus", "page", 4, "/system/menus", null,
                permissionSet("system:menu:list")));
        menuRepository.save(createMenu(systemManagement.getId(), "Settings", "page", 5, "/system/settings", null, null));

        // --- Button-type menu items for CRUD permissions ---
        // Permission CRUD buttons under Permissions menu
        menuRepository.save(createMenu(permissions.getId(), "Create Permission", "button", 1, null, null,
                permissionSet("system:permission:create")));
        menuRepository.save(createMenu(permissions.getId(), "Update Permission", "button", 2, null, null,
                permissionSet("system:permission:update")));
        menuRepository.save(createMenu(permissions.getId(), "Delete Permission", "button", 3, null, null,
                permissionSet("system:permission:delete")));

        // Menu CRUD buttons under Menus menu
        menuRepository.save(createMenu(menus.getId(), "Create Menu", "button", 1, null, null,
                permissionSet("system:menu:create")));
        menuRepository.save(createMenu(menus.getId(), "Update Menu", "button", 2, null, null,
                permissionSet("system:menu:update")));
        menuRepository.save(createMenu(menus.getId(), "Delete Menu", "button", 3, null, null,
                permissionSet("system:menu:delete")));

        // System user/role update buttons
        menuRepository.save(createMenu(users.getId(), "Update User", "button", 1, null, null,
                permissionSet("system:user:update")));
        menuRepository.save(createMenu(roles.getId(), "Update Role", "button", 1, null, null,
                permissionSet("system:role:update")));
    }

    private AdminMenu createMenu(Long parentId, String name, String menuType, Integer sortOrder,
                                 String path, String icon, Set<AdminPermission> permissions) {
        AdminMenu menu = new AdminMenu(name, menuType, sortOrder);
        menu.setParentId(parentId);
        menu.setPath(path);
        menu.setIcon(icon);
        if (permissions != null) {
            menu.getPermissions().addAll(permissions);
        }
        return menu;
    }

    private Set<AdminPermission> permissionSet(String... codes) {
        return java.util.Arrays.stream(codes)
                .map(code -> permissionRepository.findByCode(code).orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
    }
}

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

import java.util.Optional;
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
        // Idempotent: create menus only if they don't already exist.
        // Uses findByPath (for page menus with paths) or findByNameAndParentId (for groups/buttons)
        // to skip existing entries, making this safe to re-run without data loss.

        // Top-level menus
        findOrCreate(null, "仪表盘", "page", 1, "/", "Dashboard", null);

        AdminMenu contentManagement = findOrCreate(null, "内容管理", "group", 2, null, "BookOpen", null);
        AdminMenu businessManagement = findOrCreate(null, "商业管理", "group", 3, null, "Briefcase", null);
        AdminMenu systemManagement = findOrCreate(null, "系统管理", "group", 4, null, "Settings", null);

        // --- Content Management children ---
        findOrCreate(contentManagement.getId(), "页面管理", "page", 1, "/cms/pages", "FileText", null);
        findOrCreate(contentManagement.getId(), "媒体资源", "page", 2, "/assets", "Image", null);

        // --- Business Management children ---
        findOrCreate(businessManagement.getId(), "套餐组", "page", 1, "/products/plan-groups", "Layers", null);
        findOrCreate(businessManagement.getId(), "套餐列表", "page", 2, "/products/plans", "Pricetags", null);
        findOrCreate(businessManagement.getId(), "订单管理", "page", 3, "/orders", "Cart", null);
        findOrCreate(businessManagement.getId(), "支付记录", "page", 4, "/payments/transactions", "Wallet", null);

        // --- System Management children ---
        AdminMenu users = findOrCreate(systemManagement.getId(), "用户管理", "page", 1, "/system/users", "People",
                permissionSet("system:user:list"));
        AdminMenu roles = findOrCreate(systemManagement.getId(), "角色管理", "page", 2, "/system/roles", "ShieldCheckmark",
                permissionSet("system:role:list"));
        AdminMenu permissions = findOrCreate(systemManagement.getId(), "权限管理", "page", 3, "/system/permissions", "Key",
                permissionSet("system:permission:list"));
        AdminMenu menus = findOrCreate(systemManagement.getId(), "菜单管理", "page", 4, "/system/menus", "Menu",
                permissionSet("system:menu:list"));
        findOrCreate(systemManagement.getId(), "系统监控", "page", 5, "/system/monitor", "Server", null);
        findOrCreate(systemManagement.getId(), "系统设置", "page", 6, "/system/settings", "Wrench", null);
        findOrCreate(systemManagement.getId(), "审计日志", "page", 7, "/system/logs", "Newspaper",
                permissionSet("system:log:list"));
        findOrCreate(systemManagement.getId(), "操作日志", "page", 8, "/system/operation-logs", "Build",
                permissionSet("system:operation-log"));
        findOrCreate(systemManagement.getId(), "访问日志", "page", 9, "/system/access-logs", "Code",
                permissionSet("system:access-log"));

        // --- Button-type menu items for CRUD permissions ---
        // Permission CRUD buttons under Permissions menu
        findOrCreate(permissions.getId(), "新建权限", "button", 1, null, null,
                permissionSet("system:permission:create"));
        findOrCreate(permissions.getId(), "编辑权限", "button", 2, null, null,
                permissionSet("system:permission:update"));
        findOrCreate(permissions.getId(), "删除权限", "button", 3, null, null,
                permissionSet("system:permission:delete"));

        // Menu CRUD buttons under Menus menu
        findOrCreate(menus.getId(), "新建菜单", "button", 1, null, null,
                permissionSet("system:menu:create"));
        findOrCreate(menus.getId(), "编辑菜单", "button", 2, null, null,
                permissionSet("system:menu:update"));
        findOrCreate(menus.getId(), "删除菜单", "button", 3, null, null,
                permissionSet("system:menu:delete"));

        // System user/role update buttons
        findOrCreate(users.getId(), "编辑用户", "button", 1, null, null,
                permissionSet("system:user:update"));
        findOrCreate(roles.getId(), "编辑角色", "button", 1, null, null,
                permissionSet("system:role:update"));
    }

    private AdminMenu findOrCreate(Long parentId, String name, String menuType, Integer sortOrder,
                                    String path, String icon, Set<AdminPermission> permissions) {
        if (path != null && !path.isBlank()) {
            Optional<AdminMenu> existing = menuRepository.findByPath(path);
            if (existing.isPresent()) return existing.get();
        }
        if (parentId != null) {
            Optional<AdminMenu> existing = menuRepository.findByNameAndParentId(name, parentId);
            if (existing.isPresent()) return existing.get();
        }
        AdminMenu menu = new AdminMenu(name, menuType, sortOrder);
        menu.setParentId(parentId);
        menu.setPath(path);
        menu.setIcon(icon);
        if (permissions != null && !permissions.isEmpty()) {
            menu.getPermissions().addAll(permissions);
        }
        return menuRepository.save(menu);
    }

    private Set<AdminPermission> permissionSet(String... codes) {
        return java.util.Arrays.stream(codes)
                .map(code -> permissionRepository.findByCode(code).orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
    }
}

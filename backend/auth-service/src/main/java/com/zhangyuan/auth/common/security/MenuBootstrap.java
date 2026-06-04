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
        // Clear existing menu data and re-seed (dev mode)
        menuRepository.deleteAll();

        // Top-level menus
        AdminMenu dashboard = menuRepository.save(createMenu(null, "仪表盘", "page", 1, "/", "Dashboard", null));

        AdminMenu contentManagement = menuRepository.save(createMenu(null, "内容管理", "group", 2, null, "BookOpen", null));
        AdminMenu businessManagement = menuRepository.save(createMenu(null, "商业管理", "group", 3, null, "Briefcase", null));
        AdminMenu systemManagement = menuRepository.save(createMenu(null, "系统管理", "group", 4, null, "Settings", null));

        // --- Content Management children ---
        menuRepository.save(createMenu(contentManagement.getId(), "页面管理", "page", 1, "/cms/pages", "FileText", null));
        menuRepository.save(createMenu(contentManagement.getId(), "媒体资源", "page", 2, "/assets", "Image", null));

        // --- Business Management children ---
        menuRepository.save(createMenu(businessManagement.getId(), "套餐组", "page", 1, "/products/plan-groups", "Layers", null));
        menuRepository.save(createMenu(businessManagement.getId(), "套餐列表", "page", 2, "/products/plans", "Pricetags", null));
        menuRepository.save(createMenu(businessManagement.getId(), "订单管理", "page", 3, "/orders", "Cart", null));
        menuRepository.save(createMenu(businessManagement.getId(), "支付记录", "page", 4, "/payments/transactions", "Wallet", null));

        // --- System Management children ---
        AdminMenu users = menuRepository.save(createMenu(systemManagement.getId(), "用户管理", "page", 1, "/system/users", "People",
                permissionSet("system:user:list")));
        AdminMenu roles = menuRepository.save(createMenu(systemManagement.getId(), "角色管理", "page", 2, "/system/roles", "ShieldCheckmark",
                permissionSet("system:role:list")));
        AdminMenu permissions = menuRepository.save(createMenu(systemManagement.getId(), "权限管理", "page", 3, "/system/permissions", "Key",
                permissionSet("system:permission:list")));
        AdminMenu menus = menuRepository.save(createMenu(systemManagement.getId(), "菜单管理", "page", 4, "/system/menus", "Menu",
                permissionSet("system:menu:list")));
        menuRepository.save(createMenu(systemManagement.getId(), "系统设置", "page", 5, "/system/settings", "Wrench", null));

        // --- Button-type menu items for CRUD permissions ---
        // Permission CRUD buttons under Permissions menu
        menuRepository.save(createMenu(permissions.getId(), "新建权限", "button", 1, null, null,
                permissionSet("system:permission:create")));
        menuRepository.save(createMenu(permissions.getId(), "编辑权限", "button", 2, null, null,
                permissionSet("system:permission:update")));
        menuRepository.save(createMenu(permissions.getId(), "删除权限", "button", 3, null, null,
                permissionSet("system:permission:delete")));

        // Menu CRUD buttons under Menus menu
        menuRepository.save(createMenu(menus.getId(), "新建菜单", "button", 1, null, null,
                permissionSet("system:menu:create")));
        menuRepository.save(createMenu(menus.getId(), "编辑菜单", "button", 2, null, null,
                permissionSet("system:menu:update")));
        menuRepository.save(createMenu(menus.getId(), "删除菜单", "button", 3, null, null,
                permissionSet("system:menu:delete")));

        // System user/role update buttons
        menuRepository.save(createMenu(users.getId(), "编辑用户", "button", 1, null, null,
                permissionSet("system:user:update")));
        menuRepository.save(createMenu(roles.getId(), "编辑角色", "button", 1, null, null,
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

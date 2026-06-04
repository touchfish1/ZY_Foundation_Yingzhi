package com.zhangyuan.auth.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.auth.application.service.MenuApplicationService;
import com.zhangyuan.auth.common.ApiResponse;
import com.zhangyuan.auth.dto.MenuRequest;
import com.zhangyuan.auth.dto.MenuResponse;
import com.zhangyuan.auth.dto.SortItem;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台菜单管理控制器，提供菜单的增删改查及排序接口。
 */
@RestController
@RequestMapping("/admin/system/menus")
public class AdminMenuController {

    private static final Logger log = LoggerFactory.getLogger(AdminMenuController.class);

    private final MenuApplicationService menuApplicationService;

    public AdminMenuController(MenuApplicationService menuApplicationService) {
        this.menuApplicationService = menuApplicationService;
    }

    @GetMapping
    @SaCheckPermission("system:menu:list")
    public ApiResponse<List<MenuResponse>> listMenus() {
        log.info("Listing all menus");
        return ApiResponse.ok(menuApplicationService.getMenuTree());
    }

    @PostMapping
    @SaCheckPermission("system:menu:create")
    public ApiResponse<MenuResponse> createMenu(@Valid @RequestBody MenuRequest request) {
        log.info("Creating menu: name={}, menuType={}", request.name(), request.menuType());
        return ApiResponse.ok(menuApplicationService.createMenu(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:menu:update")
    public ApiResponse<MenuResponse> updateMenu(@PathVariable Long id,
                                                @Valid @RequestBody MenuRequest request) {
        log.info("Updating menu: id={}", id);
        return ApiResponse.ok(menuApplicationService.updateMenu(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:menu:delete")
    public ApiResponse<Void> deleteMenu(@PathVariable Long id) {
        log.info("Deleting menu and descendants: id={}", id);
        menuApplicationService.deleteMenu(id);
        log.info("Menu and descendants deleted: id={}", id);
        return ApiResponse.ok();
    }

    @PutMapping("/sort")
    @SaCheckPermission("system:menu:update")
    public ApiResponse<Void> updateSortOrder(@Valid @RequestBody List<SortItem> sortItems) {
        log.info("Updating sort orders for {} menus", sortItems.size());
        menuApplicationService.updateSort(sortItems);
        log.info("Sort orders updated");
        return ApiResponse.ok();
    }
}

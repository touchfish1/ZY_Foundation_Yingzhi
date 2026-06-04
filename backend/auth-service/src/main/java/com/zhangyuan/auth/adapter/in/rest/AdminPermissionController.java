package com.zhangyuan.auth.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.auth.application.service.PermissionApplicationService;
import com.zhangyuan.auth.common.ApiResponse;
import com.zhangyuan.auth.dto.PermissionRequest;
import com.zhangyuan.auth.dto.PermissionResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台权限管理控制器，提供权限的增删改查接口。
 */
@RestController
@RequestMapping("/admin/system/permissions")
@SaCheckPermission("system:permission:list")
public class AdminPermissionController {

    private static final Logger log = LoggerFactory.getLogger(AdminPermissionController.class);

    private final PermissionApplicationService permissionApplicationService;

    public AdminPermissionController(PermissionApplicationService permissionApplicationService) {
        this.permissionApplicationService = permissionApplicationService;
    }

    @GetMapping
    public ApiResponse<List<PermissionResponse>> listPermissions(
            @RequestParam(required = false) String module) {
        log.info("Listing permissions, module filter={}", module);
        return ApiResponse.ok(permissionApplicationService.listPermissions(module));
    }

    @GetMapping("/modules")
    public ApiResponse<List<String>> listModules() {
        log.info("Listing permission modules");
        return ApiResponse.ok(permissionApplicationService.listModules());
    }

    @PostMapping
    @SaCheckPermission("system:permission:create")
    public ApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        log.info("Creating permission: code={}, name={}, module={}", request.code(), request.name(), request.module());
        try {
            PermissionResponse response = permissionApplicationService.createPermission(request);
            log.info("Permission created: id={}, code={}", response.id(), response.code());
            return ApiResponse.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create permission: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:permission:update")
    public ApiResponse<PermissionResponse> updatePermission(
            @PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        log.info("Updating permission: id={}", id);
        try {
            PermissionResponse response = permissionApplicationService.updatePermission(id, request);
            log.info("Permission updated: {}", id);
            return ApiResponse.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update permission: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:permission:delete")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        log.info("Deleting permission: {}", id);
        permissionApplicationService.deletePermission(id);
        log.info("Permission deleted: {}", id);
        return ApiResponse.ok();
    }
}

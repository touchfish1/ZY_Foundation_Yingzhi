package com.zhangyuan.modules.asset.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.modules.asset.application.service.AssetApplicationService;
import com.zhangyuan.modules.asset.dto.AssetFileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 后台文件资产控制器，提供文件列表和上传接口。
 */
@RestController
@RequestMapping("/admin/assets")
public class AdminAssetController {

    private static final Logger log = LoggerFactory.getLogger(AdminAssetController.class);

    private final AssetApplicationService assetApplicationService;

    public AdminAssetController(AssetApplicationService assetApplicationService) {
        this.assetApplicationService = assetApplicationService;
    }

    @GetMapping("/files")
    @SaCheckPermission("asset:list")
    public ApiResponse<List<AssetFileInfo>> listFiles() {
        log.info("Listing asset files");
        return ApiResponse.ok(assetApplicationService.listFiles());
    }

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            "application/pdf",
            "text/plain", "text/markdown",
            "application/json"
    );

    @PostMapping("/files")
    @SaCheckPermission("asset:list")
    public ApiResponse<AssetFileInfo> upload(@RequestParam("file") MultipartFile file) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser user = (AuthUser) authentication.getPrincipal();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制(最大50MB)");
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }

        log.info("Uploading file: originalName={}, userId={}", file.getOriginalFilename(), user.getId());
        return ApiResponse.ok(assetApplicationService.upload(file, user.getId()));
    }
}

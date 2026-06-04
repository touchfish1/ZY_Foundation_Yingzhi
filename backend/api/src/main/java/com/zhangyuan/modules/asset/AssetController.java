package com.zhangyuan.modules.asset;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.common.security.AuthUser;
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
 * 后台文件资产控制器，提供文件的查询和上传接口。
 */
@RestController
@RequestMapping("/admin/assets")
@SaCheckPermission("asset:list")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    /**
     * 获取文件资产列表。
     *
     * @return 文件信息列表
     */
    @GetMapping("/files")
    public ApiResponse<List<AssetFileInfo>> listFiles() {
        log.info("Listing asset files");
        return ApiResponse.ok(assetService.listFiles());
    }

    /**
     * 上传文件到 MinIO 并保存记录。
     *
     * @param file 上传的文件
     * @return 上传后的文件信息
     */
    @PostMapping("/files")
    public ApiResponse<AssetFileInfo> upload(@RequestParam("file") MultipartFile file) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser user = (AuthUser) authentication.getPrincipal();
        log.info("Uploading file: originalName={}, userId={}", file.getOriginalFilename(), user.getId());
        return ApiResponse.ok(assetService.upload(file, user.getId()));
    }
}

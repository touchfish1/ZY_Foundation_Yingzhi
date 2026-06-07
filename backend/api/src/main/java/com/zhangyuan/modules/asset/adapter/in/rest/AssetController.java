package com.zhangyuan.modules.asset.adapter.in.rest;

import com.zhangyuan.common.operationlog.annotation.OperationLog;
import static com.zhangyuan.common.operationlog.domain.model.OperationType.*;
import static com.zhangyuan.common.operationlog.domain.model.ResourceType.*;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.asset.application.service.AssetApplicationService;
import com.zhangyuan.modules.asset.domain.model.AssetFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DDD 版文件资产控制器，提供资产的查询接口。
 */
@RestController("dddAssetController")
@RequestMapping("/api/ddd/assets")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);

    private final AssetApplicationService assetApplicationService;

    public AssetController(AssetApplicationService assetApplicationService) {
        this.assetApplicationService = assetApplicationService;
    }

    /**
     * 获取所有资产文件列表。
     *
     * @return 资产文件列表
     */
    @GetMapping
    @OperationLog(type = QUERY, resource = ASSET_FILE)
    public ApiResponse<List<AssetFile>> list() {
        log.info("Listing all assets");
        return ApiResponse.ok(assetApplicationService.listAll());
    }
}

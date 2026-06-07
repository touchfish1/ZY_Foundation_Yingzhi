package com.zhangyuan.common.operationlog.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.operationlog.application.OperationLogService;
import com.zhangyuan.common.operationlog.domain.model.OperationType;
import com.zhangyuan.common.operationlog.domain.model.ResourceType;
import com.zhangyuan.common.operationlog.dto.OperationLogResponse;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.common.response.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/admin/operation-logs")
@SaCheckPermission("system:operation-log")
public class OperationLogController {

    private static final Logger log = LoggerFactory.getLogger(OperationLogController.class);

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<OperationLogResponse>> list(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) ResourceType resourceType,
            @RequestParam(required = false) OperationType operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("Listing operation logs: operatorId={}, resourceType={}, operationType={}, page={}",
                operatorId, resourceType, operationType, page);
        return ApiResponse.ok(operationLogService.query(
                operatorId, resourceType, operationType, startTime, endTime, page, pageSize));
    }
}

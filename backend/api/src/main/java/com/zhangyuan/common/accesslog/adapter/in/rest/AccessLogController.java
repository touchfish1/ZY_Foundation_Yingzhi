package com.zhangyuan.common.accesslog.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.accesslog.application.AccessLogService;
import com.zhangyuan.common.accesslog.dto.AccessLogResponse;
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
@RequestMapping("/admin/access-logs")
@SaCheckPermission("system:access-log")
public class AccessLogController {

    private static final Logger log = LoggerFactory.getLogger(AccessLogController.class);

    private final AccessLogService accessLogService;

    public AccessLogController(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AccessLogResponse>> list(
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("Listing access logs: method={}, path={}, status={}, userId={}, page={}",
                method, path, status, userId, page);
        return ApiResponse.ok(accessLogService.query(
                method, path, status, userId, startTime, endTime, page, pageSize));
    }
}

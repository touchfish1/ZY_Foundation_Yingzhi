package com.zhangyuan.user.adapter.in.rest;

import com.zhangyuan.user.application.service.AuditLogService;
import com.zhangyuan.user.common.ApiResponse;
import com.zhangyuan.user.dto.AuditLogResponse;
import com.zhangyuan.user.dto.PageResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class AuditLogController {
    private final AuditLogService auditLogService;
    public AuditLogController(AuditLogService auditLogService) { this.auditLogService = auditLogService; }

    @GetMapping("/user/{userId}")
    public ApiResponse<PageResponse<AuditLogResponse>> getUserLogs(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(PageResponse.of(auditLogService.getUserLogs(userId, page, pageSize)
                .map(e -> new AuditLogResponse(e.getId(), e.getUserId(), e.getAction(), e.getResourceType(),
                        e.getResourceId(), e.getDetail(), e.getIpAddress(), e.getCreatedAt()))));
    }

    @GetMapping
    public ApiResponse<PageResponse<AuditLogResponse>> listLogs(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(PageResponse.of(auditLogService.getAllLogs(page, pageSize)
                .map(e -> new AuditLogResponse(e.getId(), e.getUserId(), e.getAction(), e.getResourceType(),
                        e.getResourceId(), e.getDetail(), e.getIpAddress(), e.getCreatedAt()))));
    }
}

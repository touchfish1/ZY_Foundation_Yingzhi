package com.zhangyuan.order.adapter.in.rest;

import com.zhangyuan.order.application.service.UsageService;
import com.zhangyuan.order.common.ApiResponse;
import com.zhangyuan.order.dto.UsageRecordRequest;
import com.zhangyuan.order.dto.UsageRecordResponse;
import com.zhangyuan.order.dto.UsageSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/usage")
public class UsageController {
    private final UsageService usageService;
    public UsageController(UsageService usageService) { this.usageService = usageService; }

    @GetMapping("/{userId}")
    public ApiResponse<Page<UsageRecordResponse>> getUserUsage(@PathVariable Long userId,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(usageService.getUserUsage(userId, page, pageSize));
    }

    @GetMapping("/{userId}/summary")
    public ApiResponse<UsageSummaryResponse> getSummary(@PathVariable Long userId,
                                                         @RequestParam(required = false) LocalDate start,
                                                         @RequestParam(required = false) LocalDate end) {
        LocalDate s = start != null ? start : LocalDate.now().minusDays(30);
        LocalDate e = end != null ? end : LocalDate.now();
        return ApiResponse.ok(usageService.getDailySummary(userId, s, e));
    }

    @PostMapping("/record")
    public ApiResponse<Void> recordUsage(@RequestBody UsageRecordRequest request) {
        usageService.recordUsage(request.userId(), request.apiKey(), request.apiPath(),
                request.tokensIn(), request.tokensOut(), request.cost(), request.durationMs(), request.status());
        return ApiResponse.ok();
    }
}

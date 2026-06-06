package com.zhangyuan.user.adapter.in.rest;

import com.zhangyuan.user.application.service.BalanceService;
import com.zhangyuan.user.common.ApiResponse;
import com.zhangyuan.user.dto.BalanceResponse;
import com.zhangyuan.user.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {
    private static final Logger log = LoggerFactory.getLogger(BalanceController.class);
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/{userId}")
    public ApiResponse<BalanceResponse> getBalance(@PathVariable Long userId) {
        return ApiResponse.ok(new BalanceResponse(userId, balanceService.getBalance(userId)));
    }

    @GetMapping("/{userId}/transactions")
    public ApiResponse<List<TransactionResponse>> getTransactions(@PathVariable Long userId) {
        return ApiResponse.ok(balanceService.getTransactions(userId).stream()
                .map(t -> new TransactionResponse(
                        t.getId(), t.getAmount(), t.getBalanceAfter(),
                        t.getTransactionType(), t.getDescription(), t.getCreatedAt()))
                .toList());
    }

    @PostMapping("/{userId}/recharge")
    public ApiResponse<BalanceResponse> recharge(@PathVariable Long userId, @RequestParam BigDecimal amount,
                                    @RequestParam(defaultValue = "充值") String description) {
        try {
            var user = balanceService.recharge(userId, amount, description);
            return ApiResponse.ok(new BalanceResponse(userId, user.getBalance()));
        } catch (Exception e) {
            log.error("Recharge failed for userId={}, amount={}", userId, amount, e);
            return ApiResponse.error(500, "充值失败: " + e.getMessage());
        }
    }
}

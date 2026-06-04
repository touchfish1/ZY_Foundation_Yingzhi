package com.zhangyuan.system.adapter.in.rest;

import com.zhangyuan.system.application.service.BalanceService;
import com.zhangyuan.system.dto.BalanceResponse;
import com.zhangyuan.system.dto.TransactionResponse;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/{userId}")
    public BalanceResponse getBalance(@PathVariable Long userId) {
        return new BalanceResponse(userId, balanceService.getBalance(userId));
    }

    @GetMapping("/{userId}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable Long userId) {
        return balanceService.getTransactions(userId).stream()
                .map(t -> new TransactionResponse(
                        t.getId(), t.getAmount(), t.getBalanceAfter(),
                        t.getTransactionType(), t.getDescription(), t.getCreatedAt()))
                .toList();
    }

    @PostMapping("/{userId}/recharge")
    public BalanceResponse recharge(@PathVariable Long userId, @RequestParam BigDecimal amount,
                                    @RequestParam(defaultValue = "充值") String description) {
        var user = balanceService.recharge(userId, amount, description);
        return new BalanceResponse(userId, user.getBalance());
    }
}

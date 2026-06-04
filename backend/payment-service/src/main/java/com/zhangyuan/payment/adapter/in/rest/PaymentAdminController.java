package com.zhangyuan.payment.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.payment.application.service.PaymentApplicationService;
import com.zhangyuan.payment.common.ApiResponse;
import com.zhangyuan.payment.dto.PaymentResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/payments")
@SaCheckPermission("payment:list")
public class PaymentAdminController {
    private final PaymentApplicationService paymentApplicationService;
    public PaymentAdminController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @GetMapping
    public ApiResponse<List<PaymentResponse>> listPayments() {
        return ApiResponse.ok(List.of());
    }
}

package com.zhangyuan.modules.payment.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.payment.application.service.PaymentApplicationService;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/payments")
@SaCheckPermission("payment:list")
public class PaymentAdminController {

    private static final Logger log = LoggerFactory.getLogger(PaymentAdminController.class);

    private final PaymentApplicationService paymentApplicationService;

    public PaymentAdminController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @GetMapping
    public ApiResponse<List<PaymentResponse>> listPayments() {
        log.info("Listing all payments");
        return ApiResponse.ok(paymentApplicationService.listPayments());
    }
}

package com.zhangyuan.modules.payment.adapter.in.rest;

import cn.dev33.satoken.annotation.SaIgnore;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.payment.application.service.PaymentApplicationService;
import com.zhangyuan.modules.payment.dto.CheckoutRequest;
import com.zhangyuan.modules.payment.dto.CheckoutResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SaIgnore
@RequestMapping("/api/payments")
public class PaymentPublicController {

    private static final Logger log = LoggerFactory.getLogger(PaymentPublicController.class);

    private final PaymentApplicationService paymentApplicationService;

    public PaymentPublicController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping("/checkout")
    public ApiResponse<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        log.info("Checkout request: orderNo={}, channel={}", request.orderNo(), request.channel());
        return ApiResponse.ok(paymentApplicationService.checkout(request));
    }

    @PostMapping("/mock/{paymentNo}/success")
    public ApiResponse<PaymentResponse> mockSuccess(@PathVariable String paymentNo) {
        log.info("Mock payment success: {}", paymentNo);
        return ApiResponse.ok(paymentApplicationService.mockSuccess(paymentNo));
    }
}

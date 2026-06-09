package com.zhangyuan.payment.adapter.in.rest;

import com.zhangyuan.payment.application.service.PaymentApplicationService;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentPublicController {
    private static final Logger log = LoggerFactory.getLogger(PaymentPublicController.class);
    private final PaymentApplicationService paymentApplicationService;
    public PaymentPublicController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping("/checkout")
    public ApiResponse<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        log.info("Checkout: orderNo={}, channel={}", request.orderNo(), request.channel());
        return ApiResponse.ok(paymentApplicationService.checkout(request));
    }

    @PostMapping("/mock/{paymentNo}/success")
    public ApiResponse<CheckoutResponse> mockSuccess(@PathVariable String paymentNo) {
        log.info("Mock payment success: {}", paymentNo);
        return ApiResponse.ok(paymentApplicationService.mockSuccess(paymentNo));
    }
}

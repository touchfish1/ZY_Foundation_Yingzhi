package com.zhangyuan.modules.payment;

import com.zhangyuan.common.response.ApiResponse;
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

/**
 * 支付控制器，提供支付下单和模拟支付成功回调接口。
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 发起支付下单（仅支持 Mock 模拟支付）。
     *
     * @param request 下单请求
     * @return 支付下单响应
     */
    @PostMapping("/checkout")
    public ApiResponse<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        log.info("Checkout request: orderNo={}, channel={}", request.orderNo(), request.channel());
        return ApiResponse.ok(paymentService.checkout(request));
    }

    /**
     * 模拟支付成功回调。
     *
     * @param paymentNo 支付单号
     * @return 支付结果
     */
    @PostMapping("/mock/{paymentNo}/success")
    public ApiResponse<PaymentResponse> mockSuccess(@PathVariable String paymentNo) {
        log.info("Mock payment success: {}", paymentNo);
        return ApiResponse.ok(paymentService.mockSuccess(paymentNo));
    }
}

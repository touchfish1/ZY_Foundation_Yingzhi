package com.zhangyuan.modules.payment;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.payment.dto.CheckoutRequest;
import com.zhangyuan.modules.payment.dto.CheckoutResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout")
    public ApiResponse<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.ok(paymentService.checkout(request));
    }

    @PostMapping("/mock/{paymentNo}/success")
    public ApiResponse<PaymentResponse> mockSuccess(@PathVariable String paymentNo) {
        return ApiResponse.ok(paymentService.mockSuccess(paymentNo));
    }
}

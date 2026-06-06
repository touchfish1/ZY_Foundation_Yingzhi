package com.zhangyuan.modules.payment.adapter.in.rest;

import cn.dev33.satoken.annotation.SaIgnore;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.payment.application.service.PaymentApplicationService;
import com.zhangyuan.modules.payment.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DDD 版支付控制器，提供支付的查询接口。
 */
@RestController("dddPaymentController")
@SaIgnore
@RequestMapping("/api/ddd/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentApplicationService paymentApplicationService;

    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    /**
     * 根据支付单号查询支付记录。
     *
     * @param paymentNo 支付单号
     * @return 支付记录
     */
    @GetMapping("/{paymentNo}")
    public ApiResponse<Payment> get(@PathVariable String paymentNo) {
        log.info("Getting payment: {}", paymentNo);
        return paymentApplicationService.findByPaymentNo(paymentNo)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error(404, "Payment not found"));
    }

    /**
     * 获取所有支付记录列表。
     *
     * @return 支付记录列表
     */
    @GetMapping
    public ApiResponse<List<Payment>> list() {
        log.info("Listing all payments");
        return ApiResponse.ok(paymentApplicationService.listAll());
    }
}

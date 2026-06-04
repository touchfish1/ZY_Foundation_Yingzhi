package com.zhangyuan.modules.payment;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台支付管理控制器，提供支付记录的查询接口。
 */
@RestController
@RequestMapping("/admin/payments")
@SaCheckPermission("payment:list")
public class PaymentAdminController {

    private static final Logger log = LoggerFactory.getLogger(PaymentAdminController.class);

    private final PaymentService paymentService;

    public PaymentAdminController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 获取所有支付记录列表。
     *
     * @return 支付记录列表
     */
    @GetMapping
    public ApiResponse<List<PaymentResponse>> listPayments() {
        log.info("Listing all payments");
        return ApiResponse.ok(paymentService.listPayments());
    }
}

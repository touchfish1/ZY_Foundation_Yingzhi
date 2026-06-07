package com.zhangyuan.modules.payment.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.common.operationlog.annotation.OperationLog;
import static com.zhangyuan.common.operationlog.domain.model.OperationType.*;
import static com.zhangyuan.common.operationlog.domain.model.ResourceType.*;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.modules.payment.application.service.PaymentApplicationService;
import com.zhangyuan.modules.payment.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @OperationLog(type = QUERY, resource = ORDER)
    public ApiResponse<PageResponse<PaymentResponse>> listPayments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        log.info("Listing payments, page={}, pageSize={}", page, pageSize);
        return ApiResponse.ok(paymentApplicationService.listPayments(page, pageSize));
    }
}

package com.zhangyuan.payment.adapter.in.rest;

import com.zhangyuan.payment.application.service.ChannelStrategyRegistry;
import com.zhangyuan.payment.application.service.PaymentApplicationService;
import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/callbacks")
public class PaymentCallbackController {

    private static final Logger log = LoggerFactory.getLogger(PaymentCallbackController.class);
    private final PaymentRepository paymentRepository;
    private final ChannelStrategyRegistry strategyRegistry;
    private final PaymentApplicationService paymentApplicationService;

    public PaymentCallbackController(PaymentRepository paymentRepository,
                                     ChannelStrategyRegistry strategyRegistry,
                                     PaymentApplicationService paymentApplicationService) {
        this.paymentRepository = paymentRepository;
        this.strategyRegistry = strategyRegistry;
        this.paymentApplicationService = paymentApplicationService;
    }

    @PostMapping("/{channel}")
    public ApiResponse<String> handleCallback(@PathVariable String channel,
                                               @RequestParam Map<String, String> params) {
        log.info("Payment callback received for channel: {}, params: {}", channel, params);

        String rawPaymentNo = params.get("out_trade_no");
        if (rawPaymentNo == null) {
            rawPaymentNo = params.get("paymentNo");
        }
        if (rawPaymentNo == null) {
            log.warn("Callback missing paymentNo for channel: {}", channel);
            return ApiResponse.error(400, "Missing payment number");
        }
        final String paymentNo = rawPaymentNo;

        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));

        if ("SUCCESS".equals(payment.getStatus())) {
            log.warn("Idempotent callback: payment {} already SUCCESS", paymentNo);
            return ApiResponse.ok("success");
        }

        var strategy = strategyRegistry.getStrategy(channel);
        strategy.processCallback(payment, params);
        paymentApplicationService.handlePaymentSuccess(paymentNo);

        log.info("Payment callback processed: paymentNo={}, channel={}", paymentNo, channel);
        return ApiResponse.ok("success");
    }
}

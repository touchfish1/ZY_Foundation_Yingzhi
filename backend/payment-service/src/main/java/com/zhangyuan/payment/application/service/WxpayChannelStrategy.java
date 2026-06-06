package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "payment.wxpay.enabled", havingValue = "true", matchIfMissing = false)
public class WxpayChannelStrategy implements PaymentChannelStrategy {

    private static final Logger log = LoggerFactory.getLogger(WxpayChannelStrategy.class);

    public WxpayChannelStrategy() {
        log.info("WxpayChannelStrategy loaded (requires wechatpay-java on classpath for production use)");
    }

    @Override
    public String getChannel() {
        return "wxpay";
    }

    @Override
    public CheckoutResponse createPayment(Payment payment, CheckoutRequest request) {
        throw new UnsupportedOperationException(
                "微信支付需要在 pom.xml 添加 wechatpay-java 依赖并配置 payment.wxpay.* 参数后使用");
    }

    @Override
    public Payment processCallback(Payment payment, Map<String, String> callbackParams) {
        String tradeState = callbackParams.get("trade_state");
        if ("SUCCESS".equals(tradeState)) {
            payment.markSuccess();
            log.info("WeChat pay callback success: paymentNo={}, transactionId={}",
                    payment.getPaymentNo(), callbackParams.get("transaction_id"));
        }
        return payment;
    }

    @Override
    public boolean supportsAsyncCallback() {
        return true;
    }
}

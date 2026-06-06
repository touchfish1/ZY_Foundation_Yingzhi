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
@ConditionalOnProperty(name = "payment.alipay.enabled", havingValue = "true", matchIfMissing = false)
public class AlipayChannelStrategy implements PaymentChannelStrategy {

    private static final Logger log = LoggerFactory.getLogger(AlipayChannelStrategy.class);

    public AlipayChannelStrategy() {
        log.info("AlipayChannelStrategy loaded (requires alipay-sdk-java on classpath for production use)");
    }

    @Override
    public String getChannel() {
        return "alipay";
    }

    @Override
    public CheckoutResponse createPayment(Payment payment, CheckoutRequest request) {
        throw new UnsupportedOperationException(
                "支付宝支付需要在 pom.xml 添加 alipay-sdk-java 依赖并配置 payment.alipay.* 参数后使用");
    }

    @Override
    public Payment processCallback(Payment payment, Map<String, String> callbackParams) {
        String tradeStatus = callbackParams.get("trade_status");
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            payment.markSuccess();
            log.info("Alipay callback success: paymentNo={}, tradeNo={}",
                    payment.getPaymentNo(), callbackParams.get("trade_no"));
        }
        return payment;
    }

    @Override
    public boolean supportsAsyncCallback() {
        return true;
    }
}

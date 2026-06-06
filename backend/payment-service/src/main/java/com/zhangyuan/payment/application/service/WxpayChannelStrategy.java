package com.zhangyuan.payment.application.service;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "payment.wxpay.enabled", havingValue = "true", matchIfMissing = false)
public class WxpayChannelStrategy implements PaymentChannelStrategy {

    private static final Logger log = LoggerFactory.getLogger(WxpayChannelStrategy.class);

    private final NativePayService nativePayService;
    private final String appId;
    private final String mchId;
    private final String apiV3Key;
    private final String notifyUrl;

    public WxpayChannelStrategy(
            @Value("${payment.wxpay.app-id}") String appId,
            @Value("${payment.wxpay.mch-id}") String mchId,
            @Value("${payment.wxpay.api-v3-key}") String apiV3Key,
            @Value("${payment.wxpay.mch-serial-no}") String mchSerialNo,
            @Value("${payment.wxpay.private-key-path}") String privateKeyPath,
            @Value("${payment.wxpay.notify-url}") String notifyUrl) {
        this.appId = appId;
        this.mchId = mchId;
        this.apiV3Key = apiV3Key;
        this.notifyUrl = notifyUrl;

        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(mchSerialNo)
                .apiV3Key(apiV3Key)
                .build();
        this.nativePayService = new NativePayService.Builder().config(config).build();
    }

    @Override
    public String getChannel() {
        return "wxpay";
    }

    @Override
    public CheckoutResponse createPayment(Payment payment, CheckoutRequest request) {
        Amount amount = new Amount();
        amount.setTotal(payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
        amount.setCurrency("CNY");

        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(appId);
        prepayRequest.setMchid(mchId);
        prepayRequest.setDescription("AI API Service - " + payment.getPaymentNo());
        prepayRequest.setOutTradeNo(payment.getPaymentNo());
        prepayRequest.setNotifyUrl(notifyUrl);
        prepayRequest.setAmount(amount);

        PrepayResponse response = nativePayService.prepay(prepayRequest);
        String codeUrl = response.getCodeUrl();

        log.info("WeChat pay prepay created: paymentNo={}", payment.getPaymentNo());
        return new CheckoutResponse(payment.getPaymentNo(), payment.getStatus(), null, codeUrl);
    }

    @Override
    public Payment processCallback(Payment payment, Map<String, String> callbackParams) {
        String tradeState = callbackParams.get("trade_state");
        if ("SUCCESS".equals(tradeState)) {
            payment.markSuccess();
            log.info("WeChat pay callback success: paymentNo={}, transactionId={}",
                    payment.getPaymentNo(), callbackParams.get("transaction_id"));
        } else {
            log.warn("WeChat pay callback non-success: paymentNo={}, state={}",
                    payment.getPaymentNo(), tradeState);
        }
        return payment;
    }

    @Override
    public boolean supportsAsyncCallback() {
        return true;
    }
}

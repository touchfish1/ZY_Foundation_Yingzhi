package com.zhangyuan.payment.application.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component
@ConditionalOnProperty(name = "payment.alipay.enabled", havingValue = "true", matchIfMissing = false)
public class AlipayChannelStrategy implements PaymentChannelStrategy {

    private static final Logger log = LoggerFactory.getLogger(AlipayChannelStrategy.class);
    private static final String FORMAT = "json";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    private final AlipayClient alipayClient;
    private final String alipayPublicKey;
    private final String returnUrl;
    private final String notifyUrl;

    public AlipayChannelStrategy(
            @Value("${payment.alipay.gateway-url:https://openapi.alipay.com/gateway.do}") String gatewayUrl,
            @Value("${payment.alipay.app-id}") String appId,
            @Value("${payment.alipay.private-key}") String privateKey,
            @Value("${payment.alipay.alipay-public-key}") String alipayPublicKey,
            @Value("${payment.alipay.return-url:}") String returnUrl,
            @Value("${payment.alipay.notify-url:}") String notifyUrl) {
        this.alipayClient = new DefaultAlipayClient(gatewayUrl, appId, privateKey, FORMAT, CHARSET, alipayPublicKey, SIGN_TYPE);
        this.alipayPublicKey = alipayPublicKey;
        this.returnUrl = returnUrl;
        this.notifyUrl = notifyUrl;
    }

    @Override
    public String getChannel() {
        return "alipay";
    }

    @Override
    public CheckoutResponse createPayment(Payment payment, CheckoutRequest request) {
        try {
            AlipayTradePagePayRequest alipayReq = new AlipayTradePagePayRequest();
            alipayReq.setReturnUrl(returnUrl);
            alipayReq.setNotifyUrl(notifyUrl);

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(payment.getPaymentNo());
            model.setTotalAmount(payment.getAmount().toString());
            model.setSubject("AI API Service - " + payment.getPaymentNo());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            alipayReq.setBizModel(model);

            AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayReq);
            if (response.isSuccess()) {
                log.info("Alipay payment created: paymentNo={}", payment.getPaymentNo());
                return new CheckoutResponse(payment.getPaymentNo(), payment.getStatus(), null, response.getBody());
            } else {
                log.error("Alipay create payment failed: code={}, msg={}", response.getCode(), response.getMsg());
                throw new RuntimeException("Alipay create payment failed: " + response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("Alipay API error", e);
            throw new RuntimeException("Alipay API error: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment processCallback(Payment payment, Map<String, String> callbackParams) {
        try {
            Map<String, String> sortedParams = new TreeMap<>(callbackParams);
            boolean signVerified = AlipaySignature.rsaCheckV1(sortedParams, alipayPublicKey, CHARSET, SIGN_TYPE);
            if (!signVerified) {
                log.error("Alipay callback signature verification failed: paymentNo={}", payment.getPaymentNo());
                throw new SecurityException("Alipay signature verification failed");
            }

            String tradeStatus = callbackParams.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                payment.markSuccess();
                log.info("Alipay callback success: paymentNo={}, tradeNo={}",
                        payment.getPaymentNo(), callbackParams.get("trade_no"));
            } else {
                log.warn("Alipay callback non-success status: paymentNo={}, status={}",
                        payment.getPaymentNo(), tradeStatus);
            }
        } catch (AlipayApiException e) {
            log.error("Alipay callback verification error", e);
            throw new RuntimeException("Alipay callback error", e);
        }
        return payment;
    }

    @Override
    public boolean supportsAsyncCallback() {
        return true;
    }
}

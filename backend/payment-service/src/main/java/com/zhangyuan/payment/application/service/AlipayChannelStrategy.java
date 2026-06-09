package com.zhangyuan.payment.application.service;

import com.zhangyuan.payment.config.AlipayProperties;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@Component
@ConditionalOnProperty(name = "payment.alipay.enabled", havingValue = "true", matchIfMissing = false)
public class AlipayChannelStrategy implements PaymentChannelStrategy {

    private static final Logger log = LoggerFactory.getLogger(AlipayChannelStrategy.class);

    private final AlipayProperties properties;
    private final RestTemplate restTemplate;

    public AlipayChannelStrategy(AlipayProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        log.info("AlipayChannelStrategy initialized for appId={}", properties.getAppId());
    }

    @Override
    public String getChannel() {
        return "alipay";
    }

    @Override
    public CheckoutResponse createPayment(Payment payment, CheckoutRequest request) {
        try {
            Map<String, String> bizContent = new LinkedHashMap<>();
            bizContent.put("subject", "AI API Service - " + request.orderNo());
            bizContent.put("out_trade_no", payment.getPaymentNo());
            bizContent.put("total_amount", payment.getAmount().toString());
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_id", properties.getAppId());
            params.put("method", "alipay.trade.page.pay");
            params.put("format", "JSON");
            params.put("charset", "UTF-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            params.put("version", "1.0");
            params.put("notify_url", properties.getNotifyUrl());
            params.put("return_url", properties.getReturnUrl());
            params.put("biz_content", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(bizContent));

            String signContent = buildSignContent(params);
            String sign = rsaSign(signContent, properties.getPrivateKey(), "UTF-8");
            params.put("sign", sign);

            String checkoutUrl = properties.getGatewayUrl() + "?" + buildQueryString(params);

            log.info("Alipay page pay success: paymentNo={}", payment.getPaymentNo());
            return new CheckoutResponse(payment.getPaymentNo(), payment.getStatus(), null, checkoutUrl, "alipay");
        } catch (Exception e) {
            log.error("Alipay payment error: {}", e.getMessage(), e);
            throw new RuntimeException("支付宝下单失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment processCallback(Payment payment, Map<String, String> callbackParams) {
        try {
            Map<String, String> sortedParams = new TreeMap<>(callbackParams);
            String sign = sortedParams.remove("sign");
            String signType = sortedParams.remove("sign_type");

            String signContent = buildSignContent(sortedParams);
            boolean verified = rsaCheck(signContent, sign, properties.getAlipayPublicKey());

            if (!verified) {
                log.warn("Alipay callback signature verification failed: paymentNo={}", payment.getPaymentNo());
                return payment;
            }

            String tradeStatus = callbackParams.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                payment.markSuccess();
                log.info("Alipay callback success: paymentNo={}, tradeNo={}",
                        payment.getPaymentNo(), callbackParams.get("trade_no"));
            } else {
                log.info("Alipay callback status={}: paymentNo={}", tradeStatus, payment.getPaymentNo());
            }
        } catch (Exception e) {
            log.error("Alipay callback processing failed: paymentNo={}", payment.getPaymentNo(), e);
        }
        return payment;
    }

    @Override
    public boolean supportsAsyncCallback() {
        return true;
    }

    private String buildSignContent(Map<String, String> params) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                content.append((i == 0 ? "" : "&")).append(key).append("=").append(value);
            }
        }
        return content.toString();
    }

    private String rsaSign(String content, String privateKeyPem, String charset) throws Exception {
        String key = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(privateKey);
        signature.update(content.getBytes(charset));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private boolean rsaCheck(String content, String sign, String alipayPublicKeyPem) throws Exception {
        String key = alipayPublicKeyPem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        java.security.PublicKey publicKey = kf.generatePublic(spec);

        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initVerify(publicKey);
        signature.update(content.getBytes("UTF-8"));
        return signature.verify(Base64.getDecoder().decode(sign));
    }

    private String buildQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return sb.toString();
    }
}

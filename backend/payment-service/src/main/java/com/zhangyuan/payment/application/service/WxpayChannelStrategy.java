package com.zhangyuan.payment.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangyuan.payment.config.WxpayProperties;
import com.zhangyuan.payment.domain.model.Payment;
import com.zhangyuan.payment.domain.service.PaymentChannelStrategy;
import com.zhangyuan.payment.dto.CheckoutRequest;
import com.zhangyuan.payment.dto.CheckoutResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "payment.wxpay.enabled", havingValue = "true", matchIfMissing = false)
public class WxpayChannelStrategy implements PaymentChannelStrategy {

    private static final Logger log = LoggerFactory.getLogger(WxpayChannelStrategy.class);
    private static final String WXPAY_API_BASE = "https://api.mch.weixin.qq.com/v3";

    private final WxpayProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WxpayChannelStrategy(WxpayProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        log.info("WxpayChannelStrategy initialized (Native mode) for mchId={}", properties.getMchId());
    }

    @Override
    public String getChannel() {
        return "wxpay";
    }

    @Override
    public CheckoutResponse createPayment(Payment payment, CheckoutRequest request) {
        try {
            int totalFee = payment.getAmount().multiply(BigDecimal.valueOf(100)).intValue();

            Map<String, Object> body = Map.of(
                    "appid", properties.getAppId(),
                    "mchid", properties.getMchId(),
                    "description", "AI API Service - " + request.orderNo(),
                    "out_trade_no", payment.getPaymentNo(),
                    "notify_url", properties.getNativeNotifyUrl(),
                    "amount", Map.of("total", totalFee, "currency", payment.getCurrency())
            );

            String bodyJson = objectMapper.writeValueAsString(body);
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String message = buildSignMessage("POST", "/v3/pay/transactions/native", timestamp, nonceStr, bodyJson);
            String signature = signMessage(message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", buildAuthHeader("POST", "/v3/pay/transactions/native", timestamp, nonceStr, signature));
            headers.set("User-Agent", "zhangyuan-payment/1.0");

            RequestEntity<String> requestEntity = new RequestEntity<>(bodyJson, headers,
                    org.springframework.http.HttpMethod.POST,
                    URI.create(WXPAY_API_BASE + "/pay/transactions/native"));

            ResponseEntity<Map> response = restTemplate.exchange(requestEntity, Map.class);
            Map<String, Object> respBody = response.getBody();

            if (respBody != null && respBody.containsKey("code_url")) {
                String codeUrl = (String) respBody.get("code_url");
                log.info("Wxpay native prepay success: paymentNo={}", payment.getPaymentNo());
                return new CheckoutResponse(payment.getPaymentNo(), payment.getStatus(), null, codeUrl, "wxpay");
            } else {
                log.error("Wxpay prepay failed: {}", respBody);
                throw new RuntimeException("微信支付下单失败: " + (respBody != null ? respBody.get("message") : "unknown"));
            }
        } catch (Exception e) {
            log.error("Wxpay payment error: {}", e.getMessage(), e);
            throw new RuntimeException("微信支付失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Payment processCallback(Payment payment, Map<String, String> callbackParams) {
        try {
            String tradeState = callbackParams.get("trade_state");
            if ("SUCCESS".equals(tradeState)) {
                payment.markSuccess();
                log.info("Wxpay payment success verified: paymentNo={}, transactionId={}",
                        payment.getPaymentNo(), callbackParams.get("transaction_id"));
            } else {
                log.info("Wxpay payment status: paymentNo={}, tradeState={}",
                        payment.getPaymentNo(), tradeState);
            }
        } catch (Exception e) {
            log.error("Wxpay callback query failed: paymentNo={}", payment.getPaymentNo(), e);
        }
        return payment;
    }

    @Override
    public boolean supportsAsyncCallback() {
        return true;
    }

    private String buildSignMessage(String method, String urlPath, String timestamp, String nonceStr, String body) {
        return method + "\n" + urlPath + "\n" + timestamp + "\n" + nonceStr + "\n" + body + "\n";
    }

    private String signMessage(String message) throws Exception {
        java.security.PrivateKey privateKey = loadPrivateKey();
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(message.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    private String buildAuthHeader(String method, String urlPath, String timestamp, String nonceStr, String signature) {
        String merchantId = properties.getMchId();
        String serialNo = properties.getMchSerialNo();
        return "WECHATPAY2-SHA256-RSA2048 "
                + "mchid=\"" + merchantId + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + serialNo + "\","
                + "signature=\"" + signature + "\"";
    }

    private java.security.PrivateKey loadPrivateKey() throws Exception {
        String keyPem = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get(properties.getPrivateKeyPath())));
        keyPem = keyPem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyPem);
        java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}

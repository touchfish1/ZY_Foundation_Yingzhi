package com.zhangyuan.payment.dto;

/**
 * @param paymentNo   支付单号
 * @param status      支付状态 (pending/success/failed)
 * @param payUrl      Mock 支付确认 URL（mock 渠道专用）
 * @param checkoutUrl 跳转支付 URL（支付宝）或二维码内容（微信 Native）
 * @param channel     支付渠道 (mock/wxpay/alipay)
 */
public record CheckoutResponse(String paymentNo, String status, String payUrl, String checkoutUrl, String channel) {

    public CheckoutResponse(String paymentNo, String status, String payUrl, String checkoutUrl) {
        this(paymentNo, status, payUrl, checkoutUrl, null);
    }
}
